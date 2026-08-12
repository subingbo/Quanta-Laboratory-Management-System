package com.ruoyi.qt.mapper;

import java.util.List;
import com.ruoyi.qt.domain.QtInterviewApplication;
import com.ruoyi.qt.domain.QtInterviewProfile;
import com.ruoyi.qt.domain.QtInterviewResult;

public interface QtInterviewMapper
{
    QtInterviewApplication selectApplicationByUserId(Long userId);

    int insertInterviewApplication(QtInterviewApplication application);

    int updateInterviewApplication(QtInterviewApplication application);

    QtInterviewProfile selectProfileByUserId(Long userId);

    int insertInterviewProfile(QtInterviewProfile profile);

    int updateInterviewProfile(QtInterviewProfile profile);

    List<QtInterviewResult> selectResultsByUserId(Long userId);

    QtInterviewResult selectResultByUserIdAndRoundId(@org.apache.ibatis.annotations.Param("userId") Long userId,
            @org.apache.ibatis.annotations.Param("roundId") Long roundId);

    int insertInterviewResult(QtInterviewResult result);

    int updateInterviewResult(QtInterviewResult result);
}
