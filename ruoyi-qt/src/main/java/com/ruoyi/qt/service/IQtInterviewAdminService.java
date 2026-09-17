package com.ruoyi.qt.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewEvaluation;
import com.ruoyi.qt.domain.QtInterviewOfferBody;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;
import com.ruoyi.qt.domain.QtStatusBody;

public interface IQtInterviewAdminService
{
    List<QtInterviewApplication> selectAdminList(QtInterviewApplication query);

    /**
     * 塔员只读查看全部报名信息（不做部门范围过滤）
     */
    List<QtInterviewApplication> selectMemberList(QtInterviewApplication query);

    QtInterviewApplication selectApplication(Long applicationId);

    QtInterviewProfile selectProfile(Long applicationId);

    List<QtInterviewResult> selectResults(Long applicationId);

    List<QtInterviewEvaluation> selectEvaluations(QtInterviewEvaluation query);

    QtInterviewEvaluation saveEvaluation(QtInterviewEvaluation evaluation, Long operatorUserId, String operator);

    QtInterviewEvaluation updateEvaluation(QtInterviewEvaluation evaluation, Long operatorUserId, String operator);

    Map<String, Object> offer(QtInterviewOfferBody body, String operator);

    Map<String, Object> updateJoinStatus(Long applicationId, QtStatusBody body, String operator);

    Map<String, Object> updateFinalStatus(Long applicationId, QtStatusBody body, String operator);

    Map<String, Object> statistics();
}
