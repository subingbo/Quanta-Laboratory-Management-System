package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工位预约记录对象 qt_workstation_reservation
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
public class QtWorkstationReservation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预约ID */
    private Long reservationId;

    /** 工位ID */
    @Excel(name = "工位ID")
    private Long workstationId;

    /** 用户ID(sys_user.user_id) */
    @Excel(name = "用户ID(sys_user.user_id)")
    private Long userId;

    /** 预约开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预约开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reserveStart;

    /** 预约结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预约结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reserveEnd;

    /** 状态(PENDING/APPROVED/CANCELED/FINISHED) */
    @Excel(name = "状态(PENDING/APPROVED/CANCELED/FINISHED)")
    private String status;

    /** 用途 */
    @Excel(name = "用途")
    private String purpose;

    public void setReservationId(Long reservationId) 
    {
        this.reservationId = reservationId;
    }

    public Long getReservationId() 
    {
        return reservationId;
    }

    public void setWorkstationId(Long workstationId) 
    {
        this.workstationId = workstationId;
    }

    public Long getWorkstationId() 
    {
        return workstationId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setReserveStart(Date reserveStart) 
    {
        this.reserveStart = reserveStart;
    }

    public Date getReserveStart() 
    {
        return reserveStart;
    }

    public void setReserveEnd(Date reserveEnd) 
    {
        this.reserveEnd = reserveEnd;
    }

    public Date getReserveEnd() 
    {
        return reserveEnd;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setPurpose(String purpose) 
    {
        this.purpose = purpose;
    }

    public String getPurpose() 
    {
        return purpose;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("reservationId", getReservationId())
            .append("workstationId", getWorkstationId())
            .append("userId", getUserId())
            .append("reserveStart", getReserveStart())
            .append("reserveEnd", getReserveEnd())
            .append("status", getStatus())
            .append("purpose", getPurpose())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
