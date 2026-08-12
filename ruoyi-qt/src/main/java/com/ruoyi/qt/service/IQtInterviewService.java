package com.ruoyi.qt.service;

import java.util.List;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;

public interface IQtInterviewService
{
    int saveMyApplication(QtInterviewApplication application, QtInterviewProfile profile);

    QtInterviewApplication selectMyApplication(Long userId);

    QtInterviewProfile selectMyProfile(Long userId);

    List<QtInterviewResult> selectMyResultList(Long userId);

    /**
     * 录入/更新面试轮次结果（联调最小能力）
     */
    int saveInterviewResult(QtInterviewResult result);
}
