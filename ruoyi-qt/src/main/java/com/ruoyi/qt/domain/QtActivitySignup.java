package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 活动报名对象 qt_activity_signup
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
public class QtActivitySignup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 报名ID */
    private Long signupId;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 状态(APPLIED/APPROVED/CANCELED/REJECTED) */
    @Excel(name = "状态(APPLIED/APPROVED/CANCELED/REJECTED)")
    private String status;

    /** 报名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "报名时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date signupTime;

    /** 取消时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "取消时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date cancelTime;

    public void setSignupId(Long signupId) 
    {
        this.signupId = signupId;
    }

    public Long getSignupId() 
    {
        return signupId;
    }

    public void setActivityId(Long activityId) 
    {
        this.activityId = activityId;
    }

    public Long getActivityId() 
    {
        return activityId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setSignupTime(Date signupTime) 
    {
        this.signupTime = signupTime;
    }

    public Date getSignupTime() 
    {
        return signupTime;
    }

    public void setCancelTime(Date cancelTime) 
    {
        this.cancelTime = cancelTime;
    }

    public Date getCancelTime() 
    {
        return cancelTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("signupId", getSignupId())
            .append("activityId", getActivityId())
            .append("userId", getUserId())
            .append("status", getStatus())
            .append("signupTime", getSignupTime())
            .append("cancelTime", getCancelTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
