package com.ruoyi.qt.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 面试投递主表对象 qt_interview_application
 */
public class QtInterviewApplication extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private Long userId;
    @Excel(name = "姓名")
    private String realName;
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    private String gender;
    @Excel(name = "班级")
    private String className;
    @Excel(name = "第一志愿")
    private String firstChoice;
    @Excel(name = "第二志愿")
    private String secondChoice;
    private String photoUrl;
    @Excel(name = "投递状态")
    private String applyStatus;
    private String photoAccessUrl;
    @Excel(name = "录用部门")
    private String offeredDepartment;
    @Excel(name = "入职确认")
    private String joinStatus;
    @Excel(name = "最终状态")
    private String finalStatus;
    private String noticeStatus;
    @Excel(name = "学号")
    private String studentNo;
    @Excel(name = "电话")
    private String phonenumber;
    @Excel(name = "昵称")
    private String nickName;
    @Excel(name = "第一志愿轮次结果")
    private String firstChoiceStatus;
    @Excel(name = "第二志愿轮次结果")
    private String secondChoiceStatus;
    /** 查询用：数据范围部门 */
    private String scopedDepartment;
    /** 查询用：轮次 */
    private Long roundId;
    /** 查询用：志愿部门筛选 */
    private String department;
    /** 查询用：轮次结果状态 */
    private String resultStatus;

    public Long getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId(Long applicationId)
    {
        this.applicationId = applicationId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getFirstChoice()
    {
        return firstChoice;
    }

    public void setFirstChoice(String firstChoice)
    {
        this.firstChoice = firstChoice;
    }

    public String getSecondChoice()
    {
        return secondChoice;
    }

    public void setSecondChoice(String secondChoice)
    {
        this.secondChoice = secondChoice;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }

    public String getApplyStatus()
    {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus)
    {
        this.applyStatus = applyStatus;
    }

    public String getPhotoAccessUrl()
    {
        return photoAccessUrl;
    }

    public void setPhotoAccessUrl(String photoAccessUrl)
    {
        this.photoAccessUrl = photoAccessUrl;
    }

    public String getOfferedDepartment()
    {
        return offeredDepartment;
    }

    public void setOfferedDepartment(String offeredDepartment)
    {
        this.offeredDepartment = offeredDepartment;
    }

    public String getJoinStatus()
    {
        return joinStatus;
    }

    public void setJoinStatus(String joinStatus)
    {
        this.joinStatus = joinStatus;
    }

    public String getFinalStatus()
    {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus)
    {
        this.finalStatus = finalStatus;
    }

    public String getNoticeStatus()
    {
        return noticeStatus;
    }

    public void setNoticeStatus(String noticeStatus)
    {
        this.noticeStatus = noticeStatus;
    }

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getFirstChoiceStatus()
    {
        return firstChoiceStatus;
    }

    public void setFirstChoiceStatus(String firstChoiceStatus)
    {
        this.firstChoiceStatus = firstChoiceStatus;
    }

    public String getSecondChoiceStatus()
    {
        return secondChoiceStatus;
    }

    public void setSecondChoiceStatus(String secondChoiceStatus)
    {
        this.secondChoiceStatus = secondChoiceStatus;
    }

    public String getScopedDepartment()
    {
        return scopedDepartment;
    }

    public void setScopedDepartment(String scopedDepartment)
    {
        this.scopedDepartment = scopedDepartment;
    }

    public Long getRoundId()
    {
        return roundId;
    }

    public void setRoundId(Long roundId)
    {
        this.roundId = roundId;
    }

    public String getDepartment()
    {
        return department;
    }

    public void setDepartment(String department)
    {
        this.department = department;
    }

    public String getResultStatus()
    {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus)
    {
        this.resultStatus = resultStatus;
    }
}
