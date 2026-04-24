package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 实验室服装订单对象 qt_clothing_order
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
public class QtClothingOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 服装款式ID */
    @Excel(name = "服装款式ID")
    private Long itemId;

    /** 颜色 */
    @Excel(name = "颜色")
    private String selectedColor;

    /** 尺码 */
    @Excel(name = "尺码")
    private String selectedSize;

    /** 数量 */
    @Excel(name = "数量")
    private Long quantity;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 总价 */
    @Excel(name = "总价")
    private BigDecimal totalAmount;

    /** 固定付款码配置ID */
    @Excel(name = "固定付款码配置ID")
    private Long paymentConfigId;

    /** 付款截图路径 */
    @Excel(name = "付款截图路径")
    private String paymentProofPath;

    /** 付款时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "付款时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date paymentTime;

    /** 状态(DRAFT/SUBMITTED/APPROVED/REJECTED/CANCELED) */
    @Excel(name = "状态(DRAFT/SUBMITTED/APPROVED/REJECTED/CANCELED)")
    private String status;

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setOrderNo(String orderNo) 
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo() 
    {
        return orderNo;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setItemId(Long itemId) 
    {
        this.itemId = itemId;
    }

    public Long getItemId() 
    {
        return itemId;
    }

    public void setSelectedColor(String selectedColor) 
    {
        this.selectedColor = selectedColor;
    }

    public String getSelectedColor() 
    {
        return selectedColor;
    }

    public void setSelectedSize(String selectedSize) 
    {
        this.selectedSize = selectedSize;
    }

    public String getSelectedSize() 
    {
        return selectedSize;
    }

    public void setQuantity(Long quantity) 
    {
        this.quantity = quantity;
    }

    public Long getQuantity() 
    {
        return quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) 
    {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitPrice() 
    {
        return unitPrice;
    }

    public void setTotalAmount(BigDecimal totalAmount) 
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() 
    {
        return totalAmount;
    }

    public void setPaymentConfigId(Long paymentConfigId) 
    {
        this.paymentConfigId = paymentConfigId;
    }

    public Long getPaymentConfigId() 
    {
        return paymentConfigId;
    }

    public void setPaymentProofPath(String paymentProofPath) 
    {
        this.paymentProofPath = paymentProofPath;
    }

    public String getPaymentProofPath() 
    {
        return paymentProofPath;
    }

    public void setPaymentTime(Date paymentTime) 
    {
        this.paymentTime = paymentTime;
    }

    public Date getPaymentTime() 
    {
        return paymentTime;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("userId", getUserId())
            .append("itemId", getItemId())
            .append("selectedColor", getSelectedColor())
            .append("selectedSize", getSelectedSize())
            .append("quantity", getQuantity())
            .append("unitPrice", getUnitPrice())
            .append("totalAmount", getTotalAmount())
            .append("paymentConfigId", getPaymentConfigId())
            .append("paymentProofPath", getPaymentProofPath())
            .append("paymentTime", getPaymentTime())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
