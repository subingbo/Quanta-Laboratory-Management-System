package com.ruoyi.qt.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;
import com.ruoyi.qt.mapper.QtInterviewMapper;
import com.ruoyi.qt.service.IQtInterviewService;

@Service
public class QtInterviewServiceImpl implements IQtInterviewService
{
    @Autowired
    private QtInterviewMapper qtInterviewMapper;

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
    @CacheEvict(cacheNames = { CacheConstants.CACHE_QT_MY_RESULTS, CacheConstants.CACHE_QT_RECRUIT_STATISTICS },
            allEntries = true)
    public int saveInterviewResult(QtInterviewResult result)
    {
        QtInterviewResult old = qtInterviewMapper.selectResultByAppRoundDept(result.getApplicationId(),
                result.getRoundId(), result.getDepartment());
        if (old == null && result.getApplicationId() == null)
        {
            old = qtInterviewMapper.selectResultByUserIdAndRoundId(result.getUserId(), result.getRoundId());
        }
        if (old == null)
        {
            return qtInterviewMapper.insertInterviewResult(result);
        }
        result.setResultId(old.getResultId());
        if (result.getApplicationId() == null)
        {
            result.setApplicationId(old.getApplicationId());
        }
        return qtInterviewMapper.updateInterviewResult(result);
    }
}
