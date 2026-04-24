package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.QtWorkstationMapper;
import com.ruoyi.system.domain.QtWorkstation;
import com.ruoyi.system.service.IQtWorkstationService;

/**
 * 实验室工位Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@Service
public class QtWorkstationServiceImpl implements IQtWorkstationService 
{
    @Autowired
    private QtWorkstationMapper qtWorkstationMapper;

    /**
     * 查询实验室工位
     * 
     * @param workstationId 实验室工位主键
     * @return 实验室工位
     */
    @Override
    public QtWorkstation selectQtWorkstationByWorkstationId(Long workstationId)
    {
        return qtWorkstationMapper.selectQtWorkstationByWorkstationId(workstationId);
    }

    /**
     * 查询实验室工位列表
     * 
     * @param qtWorkstation 实验室工位
     * @return 实验室工位
     */
    @Override
    public List<QtWorkstation> selectQtWorkstationList(QtWorkstation qtWorkstation)
    {
        return qtWorkstationMapper.selectQtWorkstationList(qtWorkstation);
    }

    /**
     * 新增实验室工位
     * 
     * @param qtWorkstation 实验室工位
     * @return 结果
     */
    @Override
    public int insertQtWorkstation(QtWorkstation qtWorkstation)
    {
        qtWorkstation.setCreateTime(DateUtils.getNowDate());
        return qtWorkstationMapper.insertQtWorkstation(qtWorkstation);
    }

    /**
     * 修改实验室工位
     * 
     * @param qtWorkstation 实验室工位
     * @return 结果
     */
    @Override
    public int updateQtWorkstation(QtWorkstation qtWorkstation)
    {
        qtWorkstation.setUpdateTime(DateUtils.getNowDate());
        return qtWorkstationMapper.updateQtWorkstation(qtWorkstation);
    }

    /**
     * 批量删除实验室工位
     * 
     * @param workstationIds 需要删除的实验室工位主键
     * @return 结果
     */
    @Override
    public int deleteQtWorkstationByWorkstationIds(Long[] workstationIds)
    {
        return qtWorkstationMapper.deleteQtWorkstationByWorkstationIds(workstationIds);
    }

    /**
     * 删除实验室工位信息
     * 
     * @param workstationId 实验室工位主键
     * @return 结果
     */
    @Override
    public int deleteQtWorkstationByWorkstationId(Long workstationId)
    {
        return qtWorkstationMapper.deleteQtWorkstationByWorkstationId(workstationId);
    }
}
