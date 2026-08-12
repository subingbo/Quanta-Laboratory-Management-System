package com.ruoyi.qt.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    public QtInterviewApplication selectMyApplication(Long userId)
    {
        return qtInterviewMapper.selectApplicationByUserId(userId);
    }

    @Override
    public QtInterviewProfile selectMyProfile(Long userId)
    {
        return qtInterviewMapper.selectProfileByUserId(userId);
    }

    @Override
    public List<QtInterviewResult> selectMyResultList(Long userId)
    {
        return qtInterviewMapper.selectResultsByUserId(userId);
    }

    @Override
    @Transactional
    public int saveInterviewResult(QtInterviewResult result)
    {
        QtInterviewResult old = qtInterviewMapper.selectResultByUserIdAndRoundId(result.getUserId(), result.getRoundId());
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
