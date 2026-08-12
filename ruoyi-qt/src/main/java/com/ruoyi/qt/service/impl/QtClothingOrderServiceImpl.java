package com.ruoyi.qt.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qt.mapper.QtClothingOrderMapper;
import com.ruoyi.qt.domain.QtClothingOrder;
import com.ruoyi.qt.service.IQtClothingOrderService;

/**
 * 实验室服装订单Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@Service
public class QtClothingOrderServiceImpl implements IQtClothingOrderService 
{
    @Autowired
    private QtClothingOrderMapper qtClothingOrderMapper;

    /**
     * 查询实验室服装订单
     * 
     * @param orderId 实验室服装订单主键
     * @return 实验室服装订单
     */
    @Override
    public QtClothingOrder selectQtClothingOrderByOrderId(Long orderId)
    {
        return qtClothingOrderMapper.selectQtClothingOrderByOrderId(orderId);
    }

    /**
     * 查询实验室服装订单列表
     * 
     * @param qtClothingOrder 实验室服装订单
     * @return 实验室服装订单
     */
    @Override
    public List<QtClothingOrder> selectQtClothingOrderList(QtClothingOrder qtClothingOrder)
    {
        return qtClothingOrderMapper.selectQtClothingOrderList(qtClothingOrder);
    }

    @Override
    public List<QtClothingOrder> selectQtClothingOrderDetailList(QtClothingOrder qtClothingOrder)
    {
        return qtClothingOrderMapper.selectQtClothingOrderDetailList(qtClothingOrder);
    }

    /**
     * 新增实验室服装订单
     * 
     * @param qtClothingOrder 实验室服装订单
     * @return 结果
     */
    @Override
    public int insertQtClothingOrder(QtClothingOrder qtClothingOrder)
    {
        if (StringUtils.isEmpty(qtClothingOrder.getOrderNo()))
        {
            qtClothingOrder.setOrderNo(generateOrderNo());
        }
        // 无付款截图时只能落草稿，有截图才允许直接提交
        if (StringUtils.isEmpty(qtClothingOrder.getPaymentProofPath()))
        {
            qtClothingOrder.setStatus("DRAFT");
        }
        else if (StringUtils.isEmpty(qtClothingOrder.getStatus()))
        {
            qtClothingOrder.setStatus("SUBMITTED");
            qtClothingOrder.setPaymentTime(DateUtils.getNowDate());
        }
        else if ("SUBMITTED".equals(qtClothingOrder.getStatus()) || "APPROVED".equals(qtClothingOrder.getStatus()))
        {
            if (qtClothingOrder.getPaymentTime() == null)
            {
                qtClothingOrder.setPaymentTime(DateUtils.getNowDate());
            }
        }
        qtClothingOrder.setCreateTime(DateUtils.getNowDate());
        return qtClothingOrderMapper.insertQtClothingOrder(qtClothingOrder);
    }

    private String generateOrderNo()
    {
        String timePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "QT" + timePart + randomPart;
    }

    /**
     * 修改实验室服装订单
     * 
     * @param qtClothingOrder 实验室服装订单
     * @return 结果
     */
    @Override
    public int updateQtClothingOrder(QtClothingOrder qtClothingOrder)
    {
        qtClothingOrder.setUpdateTime(DateUtils.getNowDate());
        return qtClothingOrderMapper.updateQtClothingOrder(qtClothingOrder);
    }

    /**
     * 批量删除实验室服装订单
     * 
     * @param orderIds 需要删除的实验室服装订单主键
     * @return 结果
     */
    @Override
    public int deleteQtClothingOrderByOrderIds(Long[] orderIds)
    {
        return qtClothingOrderMapper.deleteQtClothingOrderByOrderIds(orderIds);
    }

    /**
     * 删除实验室服装订单信息
     * 
     * @param orderId 实验室服装订单主键
     * @return 结果
     */
    @Override
    public int deleteQtClothingOrderByOrderId(Long orderId)
    {
        return qtClothingOrderMapper.deleteQtClothingOrderByOrderId(orderId);
    }
}
