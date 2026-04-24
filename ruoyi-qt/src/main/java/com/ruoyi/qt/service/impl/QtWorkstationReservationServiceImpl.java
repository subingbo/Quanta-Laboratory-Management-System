package com.ruoyi.qt.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qt.mapper.QtWorkstationReservationMapper;
import com.ruoyi.qt.domain.QtWorkstationReservation;
import com.ruoyi.qt.service.IQtWorkstationReservationService;

/**
 * 工位预约记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@Service
public class QtWorkstationReservationServiceImpl implements IQtWorkstationReservationService 
{
    @Autowired
    private QtWorkstationReservationMapper qtWorkstationReservationMapper;

    /**
     * 查询工位预约记录
     * 
     * @param reservationId 工位预约记录主键
     * @return 工位预约记录
     */
    @Override
    public QtWorkstationReservation selectQtWorkstationReservationByReservationId(Long reservationId)
    {
        return qtWorkstationReservationMapper.selectQtWorkstationReservationByReservationId(reservationId);
    }

    /**
     * 查询工位预约记录列表
     * 
     * @param qtWorkstationReservation 工位预约记录
     * @return 工位预约记录
     */
    @Override
    public List<QtWorkstationReservation> selectQtWorkstationReservationList(QtWorkstationReservation qtWorkstationReservation)
    {
        return qtWorkstationReservationMapper.selectQtWorkstationReservationList(qtWorkstationReservation);
    }

    @Override
    public List<QtWorkstationReservation> selectQtWorkstationReservationDetailList(QtWorkstationReservation qtWorkstationReservation)
    {
        return qtWorkstationReservationMapper.selectQtWorkstationReservationDetailList(qtWorkstationReservation);
    }

    /**
     * 新增工位预约记录
     * 
     * @param qtWorkstationReservation 工位预约记录
     * @return 结果
     */
    @Override
    public int insertQtWorkstationReservation(QtWorkstationReservation qtWorkstationReservation)
    {
        qtWorkstationReservation.setCreateTime(DateUtils.getNowDate());
        return qtWorkstationReservationMapper.insertQtWorkstationReservation(qtWorkstationReservation);
    }

    /**
     * 修改工位预约记录
     * 
     * @param qtWorkstationReservation 工位预约记录
     * @return 结果
     */
    @Override
    public int updateQtWorkstationReservation(QtWorkstationReservation qtWorkstationReservation)
    {
        qtWorkstationReservation.setUpdateTime(DateUtils.getNowDate());
        return qtWorkstationReservationMapper.updateQtWorkstationReservation(qtWorkstationReservation);
    }

    /**
     * 批量删除工位预约记录
     * 
     * @param reservationIds 需要删除的工位预约记录主键
     * @return 结果
     */
    @Override
    public int deleteQtWorkstationReservationByReservationIds(Long[] reservationIds)
    {
        return qtWorkstationReservationMapper.deleteQtWorkstationReservationByReservationIds(reservationIds);
    }

    /**
     * 删除工位预约记录信息
     * 
     * @param reservationId 工位预约记录主键
     * @return 结果
     */
    @Override
    public int deleteQtWorkstationReservationByReservationId(Long reservationId)
    {
        return qtWorkstationReservationMapper.deleteQtWorkstationReservationByReservationId(reservationId);
    }
}
