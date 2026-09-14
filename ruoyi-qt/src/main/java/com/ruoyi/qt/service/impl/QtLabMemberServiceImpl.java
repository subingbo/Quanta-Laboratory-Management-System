package com.ruoyi.qt.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qt.domain.QtCohort;
import com.ruoyi.qt.domain.QtLabMember;
import com.ruoyi.qt.domain.QtMemberRecord;
import com.ruoyi.qt.domain.QtMemberRetainBody;
import com.ruoyi.qt.mapper.QtCohortMapper;
import com.ruoyi.qt.mapper.QtLabMemberMapper;
import com.ruoyi.qt.service.IQtLabMemberService;
import com.ruoyi.system.service.ISysUserService;

@Service
public class QtLabMemberServiceImpl implements IQtLabMemberService
{
    @Autowired
    private QtLabMemberMapper qtLabMemberMapper;

    @Autowired
    private QtCohortMapper qtCohortMapper;

    @Autowired
    private ISysUserService userService;

    @Override
    public List<QtLabMember> selectLabMemberList(QtLabMember query)
    {
        return qtLabMemberMapper.selectLabMemberList(query);
    }

    @Override
    public List<QtLabMember> selectAdminMemberList(QtLabMember query)
    {
        return qtLabMemberMapper.selectAdminMemberList(query);
    }

    @Override
    @Cacheable(cacheNames = CacheConstants.CACHE_QT_MEMBER_COHORTS, key = "'all'")
    public Map<String, Object> selectCohorts()
    {
        List<QtCohort> list = qtCohortMapper.selectCohortList(new QtCohort());
        QtCohort current = qtCohortMapper.selectCurrentCohort();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("currentCohort", current);
        List<Map<String, Object>> cohorts = new ArrayList<Map<String, Object>>();
        for (QtCohort cohort : list)
        {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", cohort.getCohortId());
            item.put("name", cohort.getCohortName());
            item.put("isCurrent", "1".equals(cohort.getIsCurrent()));
            cohorts.add(item);
        }
        data.put("cohorts", cohorts);
        return data;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_MEMBER_COHORTS, allEntries = true)
    public Map<String, Object> retain(Long userId, QtMemberRetainBody body, String operator)
    {
        if (body == null || !Boolean.TRUE.equals(body.getRetain()))
        {
            throw new ServiceException("retain 必须为 true");
        }
        QtCohort source = body.getSourceCohortId() == null ? qtCohortMapper.selectCurrentCohort()
                : qtCohortMapper.selectCohortById(body.getSourceCohortId());
        if (source == null)
        {
            throw new ServiceException("源届次不存在");
        }
        QtMemberRecord sourceRecord = qtCohortMapper.selectRecord(userId, source.getCohortId());
        if (sourceRecord == null)
        {
            throw new ServiceException("该用户在源届次中没有档案");
        }
        QtCohort target;
        boolean created = false;
        if (body.getTargetCohortId() != null)
        {
            target = qtCohortMapper.selectCohortById(body.getTargetCohortId());
            if (target == null)
            {
                throw new ServiceException("目标届次不存在");
            }
        }
        else
        {
            String nextName = nextCohortName(source);
            created = qtCohortMapper.selectCohortByName(nextName) == null;
            target = getOrCreateNextCohort(source, operator);
        }
        QtMemberRecord existing = qtCohortMapper.selectRecord(userId, target.getCohortId());
        if (existing == null)
        {
            QtMemberRecord next = new QtMemberRecord();
            next.setUserId(userId);
            next.setCohortId(target.getCohortId());
            next.setRoleCategory(sourceRecord.getRoleCategory());
            next.setMemberStatus("ACTIVE");
            next.setJoinTime(new Date());
            next.setRetainFlag("0");
            next.setCreateBy(operator);
            qtCohortMapper.insertRecord(next);
            existing = next;
        }
        sourceRecord.setMemberStatus("RETAINED");
        sourceRecord.setRetainFlag("1");
        sourceRecord.setUpdateBy(operator);
        qtCohortMapper.updateRecord(sourceRecord);

        SysUser user = userService.selectUserById(userId);
        if (user != null)
        {
            user.setMemberCohort(target.getCohortName());
            user.setIsQuantaMember("1");
            user.setUpdateBy(operator);
            userService.updateUser(user);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("sourceStatus", sourceRecord.getMemberStatus());
        data.put("targetRecord", existing);
        data.put("createdCohort", created);
        return data;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_MEMBER_COHORTS, allEntries = true)
    public void rollover()
    {
        QtCohort current = qtCohortMapper.selectCurrentCohort();
        if (current == null)
        {
            return;
        }
        QtCohort next = getOrCreateNextCohort(current, "system");
        List<QtMemberRecord> records = qtCohortMapper.selectRecordsByCohort(current.getCohortId());
        Date now = new Date();
        for (QtMemberRecord record : records)
        {
            if (!"ACTIVE".equals(record.getMemberStatus()))
            {
                continue;
            }
            String role = record.getRoleCategory();
            boolean retainConfirmed = "1".equals(record.getRetainFlag());
            if ("MGMT".equals(role) || (("MANAGER".equals(role) || "INTERN".equals(role)) && !retainConfirmed))
            {
                record.setMemberStatus("RESIGNED");
                record.setUpdateBy("system");
                qtCohortMapper.updateRecord(record);
                SysUser user = userService.selectUserById(record.getUserId());
                if (user != null)
                {
                    user.setIsQuantaMember("0");
                    user.setUpdateBy("system");
                    userService.updateUser(user);
                }
                continue;
            }
            if (retainConfirmed || "MEMBER".equals(role) || "CEO".equals(role))
            {
                record.setMemberStatus("RETAINED");
                record.setRetainFlag("1");
                record.setUpdateBy("system");
                qtCohortMapper.updateRecord(record);
                if (qtCohortMapper.selectRecord(record.getUserId(), next.getCohortId()) == null)
                {
                    QtMemberRecord nextRecord = new QtMemberRecord();
                    nextRecord.setUserId(record.getUserId());
                    nextRecord.setCohortId(next.getCohortId());
                    nextRecord.setRoleCategory(record.getRoleCategory());
                    nextRecord.setMemberStatus("ACTIVE");
                    nextRecord.setJoinTime(now);
                    nextRecord.setRetainFlag("0");
                    nextRecord.setCreateBy("system");
                    qtCohortMapper.insertRecord(nextRecord);
                }
                SysUser user = userService.selectUserById(record.getUserId());
                if (user != null)
                {
                    user.setMemberCohort(next.getCohortName());
                    user.setIsQuantaMember("1");
                    user.setUpdateBy("system");
                    userService.updateUser(user);
                }
            }
        }
        qtCohortMapper.clearCurrentFlag();
        next.setIsCurrent("1");
        next.setUpdateBy("system");
        qtCohortMapper.updateCohort(next);
        current.setIsCurrent("0");
        current.setStatus("ARCHIVED");
        current.setUpdateBy("system");
        qtCohortMapper.updateCohort(current);
    }

    private QtCohort getOrCreateNextCohort(QtCohort source, String operator)
    {
        String nextName = nextCohortName(source);
        QtCohort existing = qtCohortMapper.selectCohortByName(nextName);
        if (existing != null)
        {
            return existing;
        }
        QtCohort next = new QtCohort();
        next.setCohortName(nextName);
        next.setCohortYear(source.getCohortYear() == null ? null : source.getCohortYear() + 1);
        next.setIsCurrent("0");
        next.setStatus("ACTIVE");
        next.setCreateBy(operator);
        qtCohortMapper.insertCohort(next);
        return next;
    }

    private String nextCohortName(QtCohort source)
    {
        String name = source.getCohortName();
        if (StringUtils.isEmpty(name))
        {
            return String.valueOf(source.getCohortYear() == null ? 1 : source.getCohortYear() + 1);
        }
        Matcher matcher = Pattern.compile("(\\d+)").matcher(name);
        String lastNum = null;
        int lastStart = -1;
        int lastEnd = -1;
        while (matcher.find())
        {
            lastNum = matcher.group(1);
            lastStart = matcher.start(1);
            lastEnd = matcher.end(1);
        }
        if (lastNum == null)
        {
            return name + "-next";
        }
        int next = Integer.parseInt(lastNum) + 1;
        return name.substring(0, lastStart) + next + name.substring(lastEnd);
    }
}
