package com.ruoyi.qt.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;
import com.ruoyi.qt.domain.QtInterviewRound;
import com.ruoyi.qt.mapper.QtInterviewMapper;
import com.ruoyi.qt.service.IQtInterviewService;
import com.ruoyi.qt.util.QtInterviewRounds;

@Service
public class QtInterviewServiceImpl implements IQtInterviewService
{
    @Autowired
    private QtInterviewMapper qtInterviewMapper;

    @Autowired
    private QtInterviewNotifier interviewNotifier;

    @Override
    @Transactional
    @CacheEvict(cacheNames = { CacheConstants.CACHE_QT_MY_APPLICATION, CacheConstants.CACHE_QT_MY_PROFILE,
            CacheConstants.CACHE_QT_MY_RESULTS, CacheConstants.CACHE_QT_RECRUIT_STATISTICS }, allEntries = true)
    public int saveMyApplication(QtInterviewApplication application, QtInterviewProfile profile)
    {
        QtInterviewApplication oldApplication = qtInterviewMapper.selectApplicationByUserId(application.getUserId());
        int rows;
        if (oldApplication == null)
        {
            rows = qtInterviewMapper.insertInterviewApplication(application);
        }
        else
        {
            application.setApplicationId(oldApplication.getApplicationId());
            rows = qtInterviewMapper.updateInterviewApplication(application);
        }

        profile.setApplicationId(application.getApplicationId());
        QtInterviewProfile oldProfile = qtInterviewMapper.selectProfileByUserId(application.getUserId());
        if (oldProfile == null)
        {
            qtInterviewMapper.insertInterviewProfile(profile);
        }
        else
        {
            profile.setProfileId(oldProfile.getProfileId());
            qtInterviewMapper.updateInterviewProfile(profile);
        }
        return rows;
    }

    /**
     * 学生自查投递。缓存 30 秒：招新开放时段同一账号会连着刷「流程」页，
     * 30 秒的展示延迟可接受，而投递本身会主动清自己的缓存。
     */
    @Override
    @Cacheable(cacheNames = CacheConstants.CACHE_QT_MY_APPLICATION, key = "#userId")
    public QtInterviewApplication selectMyApplication(Long userId)
    {
        return qtInterviewMapper.selectApplicationByUserId(userId);
    }

    @Override
    @Cacheable(cacheNames = CacheConstants.CACHE_QT_MY_PROFILE, key = "#userId")
    public QtInterviewProfile selectMyProfile(Long userId)
    {
        return qtInterviewMapper.selectProfileByUserId(userId);
    }

    @Override
    @Cacheable(cacheNames = CacheConstants.CACHE_QT_MY_RESULTS, key = "#userId")
    public List<QtInterviewResult> selectMyResultList(Long userId)
    {
        return qtInterviewMapper.selectResultsByUserId(userId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = { CacheConstants.CACHE_QT_MY_RESULTS, CacheConstants.CACHE_QT_MY_APPLICATION,
            CacheConstants.CACHE_QT_RECRUIT_STATISTICS }, allEntries = true)
    public Map<String, Object> saveInterviewResult(QtInterviewResult result)
    {
        QtInterviewRound round = QtInterviewRounds.require(qtInterviewMapper, result.getRoundId());
        result.setRoundId(round.getRoundId());

        QtInterviewResult old = qtInterviewMapper.selectResultByAppRoundDept(result.getApplicationId(),
                result.getRoundId(), result.getDepartment());
        if (old == null && result.getApplicationId() == null)
        {
            old = qtInterviewMapper.selectResultByUserIdAndRoundId(result.getUserId(), result.getRoundId());
        }
        if (old == null)
        {
            qtInterviewMapper.insertInterviewResult(result);
        }
        else
        {
            result.setResultId(old.getResultId());
            if (result.getApplicationId() == null)
            {
                result.setApplicationId(old.getApplicationId());
            }
            qtInterviewMapper.updateInterviewResult(result);
        }

        boolean pass = "PASS".equalsIgnoreCase(result.getResultStatus());
        String roundName = round.getRoundNo() != null && round.getRoundNo() == 2
                ? "\u4e8c\u9762" : "\u4e00\u9762";
        String deptLabel = interviewNotifier.departmentLabel(result.getDepartment());
        String subject = roundName + "\u7ed3\u679c\u901a\u77e5";
        String content = pass
                ? ("\u4f60\u597d\uff0c\u4f60\u5df2\u901a\u8fc7 " + deptLabel + " \u90e8\u95e8" + roundName
                        + "\uff0c\u8bf7\u7559\u610f\u540e\u7eed\u5b89\u6392\u3002")
                : ("\u4f60\u597d\uff0c\u5f88\u9057\u61be\u4f60\u672a\u901a\u8fc7 " + deptLabel + " \u90e8\u95e8"
                        + roundName + "\u3002");
        boolean emailSent = interviewNotifier.notifyApplicant(result.getUserId(), subject, content);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("emailSent", emailSent);
        data.put("resultStatus", result.getResultStatus());
        data.put("roundNo", round.getRoundNo());
        data.put("department", result.getDepartment());
        return data;
    }
}
