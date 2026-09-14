package com.ruoyi.qt.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewEvaluation;
import com.ruoyi.qt.domain.QtInterviewOfferBody;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;
import com.ruoyi.qt.domain.QtInterviewRound;
import com.ruoyi.qt.domain.QtStatusBody;
import com.ruoyi.qt.mapper.QtInterviewMapper;
import com.ruoyi.qt.service.IQtInterviewAdminService;
import com.ruoyi.qt.util.QtAuthUtils;
import com.ruoyi.qt.util.QtDictUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;

@Service
public class QtInterviewAdminServiceImpl implements IQtInterviewAdminService
{
    @Autowired
    private QtInterviewMapper qtInterviewMapper;

    @Autowired
    private ISysNoticeService noticeService;

    @Override
    public List<QtInterviewApplication> selectAdminList(QtInterviewApplication query)
    {
        query.setScopedDepartment(QtAuthUtils.scopedDepartment());
        return qtInterviewMapper.selectAdminApplicationList(query);
    }

    @Override
    public QtInterviewApplication selectApplication(Long applicationId)
    {
        QtInterviewApplication application = requireApplication(applicationId);
        QtAuthUtils.assertApplicationScope(application.getFirstChoice(), application.getSecondChoice());
        return application;
    }

    @Override
    public QtInterviewProfile selectProfile(Long applicationId)
    {
        selectApplication(applicationId);
        return qtInterviewMapper.selectProfileByApplicationId(applicationId);
    }

    @Override
    public List<QtInterviewResult> selectResults(Long applicationId)
    {
        QtInterviewApplication application = selectApplication(applicationId);
        List<QtInterviewResult> list = qtInterviewMapper.selectResultsByApplicationId(applicationId);
        String scoped = QtAuthUtils.scopedDepartment();
        if (scoped == null)
        {
            return list;
        }
        list.removeIf(item -> item.getDepartment() != null && !scoped.equals(item.getDepartment())
                && !application.getFirstChoice().equals(scoped) && !application.getSecondChoice().equals(scoped));
        if (scoped != null)
        {
            list.removeIf(item -> !scoped.equals(item.getDepartment()));
        }
        return list;
    }

    @Override
    public List<QtInterviewEvaluation> selectEvaluations(QtInterviewEvaluation query)
    {
        if (query.getApplicationId() != null)
        {
            selectApplication(query.getApplicationId());
        }
        String scoped = QtAuthUtils.scopedDepartment();
        if (scoped != null && StringUtils.isEmpty(query.getDepartment()))
        {
            query.setDepartment(scoped);
        }
        else if (StringUtils.isNotEmpty(query.getDepartment()))
        {
            QtAuthUtils.assertDepartmentScope(query.getDepartment());
        }
        return qtInterviewMapper.selectEvaluationList(query);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_RECRUIT_STATISTICS, allEntries = true)
    public QtInterviewEvaluation saveEvaluation(QtInterviewEvaluation evaluation, Long operatorUserId, String operator)
    {
        validateEvaluation(evaluation);
        QtInterviewApplication application = selectApplication(evaluation.getApplicationId());
        String department = resolveEvalDepartment(evaluation.getDepartment(), application);
        evaluation.setDepartment(department);
        assertEvalRound(evaluation.getRoundId());
        QtInterviewEvaluation old = qtInterviewMapper.selectEvaluationByUnique(evaluation.getApplicationId(),
                evaluation.getRoundId(), department, operatorUserId);
        evaluation.setEvaluatorUserId(operatorUserId);
        evaluation.setUpdateBy(operator);
        if (old == null)
        {
            evaluation.setCreateBy(operator);
            qtInterviewMapper.insertEvaluation(evaluation);
            markProcessing(application, operator);
            return qtInterviewMapper.selectEvaluationById(evaluation.getEvaluationId());
        }
        old.setContent(evaluation.getContent());
        old.setUpdateBy(operator);
        qtInterviewMapper.updateEvaluation(old);
        return qtInterviewMapper.selectEvaluationById(old.getEvaluationId());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_RECRUIT_STATISTICS, allEntries = true)
    public QtInterviewEvaluation updateEvaluation(QtInterviewEvaluation evaluation, Long operatorUserId, String operator)
    {
        QtInterviewEvaluation old = qtInterviewMapper.selectEvaluationById(evaluation.getEvaluationId());
        if (old == null)
        {
            throw new ServiceException("面评不存在");
        }
        if (!operatorUserId.equals(old.getEvaluatorUserId()) && !QtAuthUtils.isCeo())
        {
            throw new ServiceException("无权修改他人面评");
        }
        selectApplication(old.getApplicationId());
        assertEvalRound(old.getRoundId());
        QtAuthUtils.assertDepartmentScope(old.getDepartment());
        if (StringUtils.isEmpty(evaluation.getContent()))
        {
            throw new ServiceException("面评内容不能为空");
        }
        old.setContent(evaluation.getContent());
        old.setUpdateBy(operator);
        qtInterviewMapper.updateEvaluation(old);
        return qtInterviewMapper.selectEvaluationById(old.getEvaluationId());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_RECRUIT_STATISTICS, allEntries = true)
    public Map<String, Object> offer(QtInterviewOfferBody body, String operator)
    {
        if (body == null || body.getApplicationId() == null)
        {
            throw new ServiceException("applicationId 不能为空");
        }
        String decision = body.getDecision() == null ? "" : body.getDecision().toUpperCase();
        if (!"PASS".equals(decision) && !"OUT".equals(decision))
        {
            throw new ServiceException("decision 只能是 PASS 或 OUT");
        }
        QtInterviewApplication application = selectApplication(body.getApplicationId());
        String department = resolveOfferDepartment(body, application);
        QtAuthUtils.assertDepartmentScope(department);
        QtInterviewRound round = body.getRoundId() == null ? qtInterviewMapper.selectRoundByNo(2)
                : qtInterviewMapper.selectRoundById(body.getRoundId());
        if (round == null)
        {
            throw new ServiceException("面试轮次不存在");
        }
        String resultStatus = "PASS".equals(decision) ? "PASS" : "OUT";
        QtDictUtils.requireValue(QtDictUtils.RESULT_STATUS, resultStatus, "decision");
        QtInterviewResult result = qtInterviewMapper.selectResultByAppRoundDept(application.getApplicationId(),
                round.getRoundId(), department);
        Date now = new Date();
        if (result == null)
        {
            result = new QtInterviewResult();
            result.setApplicationId(application.getApplicationId());
            result.setUserId(application.getUserId());
            result.setRoundId(round.getRoundId());
            result.setDepartment(department);
            result.setResultStatus(resultStatus);
            result.setPublishedTime(now);
            result.setCreateBy(operator);
            result.setUpdateBy(operator);
            qtInterviewMapper.insertInterviewResult(result);
        }
        else
        {
            result.setResultStatus(resultStatus);
            result.setPublishedTime(now);
            result.setUpdateBy(operator);
            qtInterviewMapper.updateInterviewResult(result);
        }

        if ("PASS".equals(decision))
        {
            String otherDept = department.equals(application.getFirstChoice()) ? application.getSecondChoice()
                    : application.getFirstChoice();
            QtInterviewResult other = qtInterviewMapper.selectResultByAppRoundDept(application.getApplicationId(),
                    round.getRoundId(), otherDept);
            if (other != null && "PASS".equals(other.getResultStatus()))
            {
                application.setOfferedDepartment(application.getFirstChoice());
            }
            else
            {
                application.setOfferedDepartment(department);
            }
            application.setApplyStatus("OFFERED");
            if (StringUtils.isEmpty(application.getJoinStatus()))
            {
                application.setJoinStatus("PENDING");
            }
        }
        else
        {
            String otherDept = department.equals(application.getFirstChoice()) ? application.getSecondChoice()
                    : application.getFirstChoice();
            QtInterviewResult other = qtInterviewMapper.selectResultByAppRoundDept(application.getApplicationId(),
                    round.getRoundId(), otherDept);
            boolean otherOut = other != null
                    && ("OUT".equals(other.getResultStatus()) || "FAIL".equals(other.getResultStatus()));
            application.setApplyStatus(otherOut ? "REJECTED" : "PROCESSING");
        }
        application.setNoticeStatus("SENT");
        application.setUpdateBy(operator);
        qtInterviewMapper.updateApplicationAdminFields(application);

        SysNotice notice = new SysNotice();
        boolean pass = "PASS".equals(decision);
        notice.setNoticeTitle(pass ? "录用通知" : "淘汰通知");
        notice.setNoticeType("1");
        notice.setStatus("0");
        String content = StringUtils.isNotEmpty(body.getNotice()) ? body.getNotice()
                : (pass ? ("恭喜你通过 " + application.getOfferedDepartment() + " 部门面试，请留意后续安排")
                        : ("很遗憾，你未通过 " + department + " 部门面试"));
        notice.setNoticeContent(content);
        notice.setCreateBy(operator);
        notice.setRemark("qt_offer:" + application.getUserId());
        noticeService.insertNotice(notice);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("applyStatus", application.getApplyStatus());
        data.put("offeredDepartment", application.getOfferedDepartment());
        data.put("noticeStatus", application.getNoticeStatus());
        data.put("resultStatus", resultStatus);
        data.put("department", department);
        return data;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_RECRUIT_STATISTICS, allEntries = true)
    public Map<String, Object> updateJoinStatus(Long applicationId, QtStatusBody body, String operator)
    {
        QtInterviewApplication application = selectApplication(applicationId);
        if (body == null || StringUtils.isEmpty(body.getJoinStatus()))
        {
            throw new ServiceException("joinStatus 不能为空");
        }
        String status = body.getJoinStatus().toUpperCase();
        if (!"PENDING".equals(status) && !"ACCEPTED".equals(status) && !"DECLINED".equals(status))
        {
            throw new ServiceException("joinStatus 只能是 PENDING/ACCEPTED/DECLINED");
        }
        application.setJoinStatus(status);
        application.setUpdateBy(operator);
        if (StringUtils.isNotEmpty(body.getRemark()))
        {
            application.setRemark(body.getRemark());
        }
        qtInterviewMapper.updateApplicationAdminFields(application);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("joinStatus", status);
        return data;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_RECRUIT_STATISTICS, allEntries = true)
    public Map<String, Object> updateFinalStatus(Long applicationId, QtStatusBody body, String operator)
    {
        QtInterviewApplication application = selectApplication(applicationId);
        if (body == null || StringUtils.isEmpty(body.getFinalStatus()))
        {
            throw new ServiceException("finalStatus 不能为空");
        }
        QtDictUtils.requireValue(QtDictUtils.APPLY_STATUS, body.getFinalStatus(), "finalStatus");
        application.setFinalStatus(body.getFinalStatus());
        application.setApplyStatus(body.getFinalStatus());
        application.setUpdateBy(operator);
        qtInterviewMapper.updateApplicationAdminFields(application);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("finalStatus", application.getFinalStatus());
        data.put("applyStatus", application.getApplyStatus());
        return data;
    }

    @Override
    @Cacheable(cacheNames = CacheConstants.CACHE_QT_RECRUIT_STATISTICS,
            key = "T(com.ruoyi.qt.cache.QtCacheKeys).scopedDept()")
    public Map<String, Object> statistics()
    {
        String scoped = QtAuthUtils.scopedDepartment();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("submittedCount", 0);
        data.put("processingCount", 0);
        data.put("offeredCount", 0);
        data.put("rejectedCount", 0);
        List<Map<String, Object>> rows = qtInterviewMapper.selectApplyStatusCounts(scoped);
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                String status = String.valueOf(row.get("applyStatus"));
                int cnt = ((Number) row.get("cnt")).intValue();
                if ("SUBMITTED".equals(status))
                {
                    data.put("submittedCount", cnt);
                }
                else if ("PROCESSING".equals(status))
                {
                    data.put("processingCount", cnt);
                }
                else if ("OFFERED".equals(status))
                {
                    data.put("offeredCount", cnt);
                }
                else if ("REJECTED".equals(status))
                {
                    data.put("rejectedCount", cnt);
                }
            }
        }
        data.put("pendingEvalCount", qtInterviewMapper.countPendingEval(scoped));
        data.put("pendingOfferCount", qtInterviewMapper.countPendingOffer(scoped));
        data.put("todayResumeCount", qtInterviewMapper.countTodayApplications());
        data.put("submissions", qtInterviewMapper.countTotalApplications(scoped));
        data.put("firstPassed", qtInterviewMapper.countRoundPassed(scoped, 1));
        data.put("secondPassed", qtInterviewMapper.countRoundPassed(scoped, 2));
        data.put("joined", qtInterviewMapper.countJoined(scoped));
        return data;
    }

    private QtInterviewApplication requireApplication(Long applicationId)
    {
        QtInterviewApplication application = qtInterviewMapper.selectApplicationById(applicationId);
        if (application == null)
        {
            throw new ServiceException("申请不存在");
        }
        return application;
    }

    private void validateEvaluation(QtInterviewEvaluation evaluation)
    {
        if (evaluation.getApplicationId() == null)
        {
            throw new ServiceException("applicationId 不能为空");
        }
        if (evaluation.getRoundId() == null)
        {
            throw new ServiceException("roundId 不能为空");
        }
        if (StringUtils.isEmpty(evaluation.getContent()))
        {
            throw new ServiceException("面评内容不能为空");
        }
    }

    private String resolveEvalDepartment(String department, QtInterviewApplication application)
    {
        if (StringUtils.isNotEmpty(department))
        {
            QtAuthUtils.assertDepartmentScope(department);
            if (!department.equals(application.getFirstChoice()) && !department.equals(application.getSecondChoice()))
            {
                throw new ServiceException("该部门不在候选人志愿中");
            }
            return department;
        }
        String scoped = QtAuthUtils.scopedDepartment();
        if (scoped != null)
        {
            return scoped;
        }
        return application.getFirstChoice();
    }

    private void assertEvalRound(Long roundId)
    {
        if (!QtAuthUtils.isManagerOnly())
        {
            return;
        }
        QtInterviewRound round = qtInterviewMapper.selectRoundById(roundId);
        if (round == null || round.getRoundNo() == null || round.getRoundNo() != 2)
        {
            throw new ServiceException("经理层只能评本部门二面");
        }
    }

    private void markProcessing(QtInterviewApplication application, String operator)
    {
        if ("SUBMITTED".equals(application.getApplyStatus()))
        {
            application.setApplyStatus("PROCESSING");
            application.setUpdateBy(operator);
            qtInterviewMapper.updateApplicationAdminFields(application);
        }
    }

    private String resolveOfferDepartment(QtInterviewOfferBody body, QtInterviewApplication application)
    {
        if (body.getVolunteerNo() != null)
        {
            if (body.getVolunteerNo() == 1)
            {
                return application.getFirstChoice();
            }
            if (body.getVolunteerNo() == 2)
            {
                return application.getSecondChoice();
            }
            throw new ServiceException("volunteerNo 只能是 1 或 2");
        }
        if (StringUtils.isNotEmpty(body.getDepartment()))
        {
            return body.getDepartment();
        }
        String scoped = QtAuthUtils.scopedDepartment();
        return scoped != null ? scoped : application.getFirstChoice();
    }
}
