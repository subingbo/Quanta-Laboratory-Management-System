package com.ruoyi.qt.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 面试投递主表对象 qt_interview_application
 */
public class QtInterviewApplication extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private Long userId;
    private String realName;
    private String gender;
    private String className;
    private String firstChoice;
    private String secondChoice;
    private String photoUrl;
    private String applyStatus;
    private String photoAccessUrl;

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
}
