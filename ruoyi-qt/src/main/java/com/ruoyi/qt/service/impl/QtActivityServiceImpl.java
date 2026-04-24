package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.QtActivityMapper;
import com.ruoyi.system.domain.QtActivity;
import com.ruoyi.system.service.IQtActivityService;

/**
 * 实验室活动Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@Service
public class QtActivityServiceImpl implements IQtActivityService 
{
    @Autowired
    private QtActivityMapper qtActivityMapper;

    /**
     * 查询实验室活动
     * 
     * @param activityId 实验室活动主键
     * @return 实验室活动
     */
    @Override
    public QtActivity selectQtActivityByActivityId(Long activityId)
    {
        return qtActivityMapper.selectQtActivityByActivityId(activityId);
    }

    /**
     * 查询实验室活动列表
     * 
     * @param qtActivity 实验室活动
     * @return 实验室活动
     */
    @Override
    public List<QtActivity> selectQtActivityList(QtActivity qtActivity)
    {
        return qtActivityMapper.selectQtActivityList(qtActivity);
    }

    /**
     * 新增实验室活动
     * 
     * @param qtActivity 实验室活动
     * @return 结果
     */
    @Override
    public int insertQtActivity(QtActivity qtActivity)
    {
        qtActivity.setCreateTime(DateUtils.getNowDate());
        return qtActivityMapper.insertQtActivity(qtActivity);
    }

    /**
     * 修改实验室活动
     * 
     * @param qtActivity 实验室活动
     * @return 结果
     */
    @Override
    public int updateQtActivity(QtActivity qtActivity)
    {
        qtActivity.setUpdateTime(DateUtils.getNowDate());
        return qtActivityMapper.updateQtActivity(qtActivity);
    }

    /**
     * 批量删除实验室活动
     * 
     * @param activityIds 需要删除的实验室活动主键
     * @return 结果
     */
    @Override
    public int deleteQtActivityByActivityIds(Long[] activityIds)
    {
        return qtActivityMapper.deleteQtActivityByActivityIds(activityIds);
    }

    /**
     * 删除实验室活动信息
     * 
     * @param activityId 实验室活动主键
     * @return 结果
     */
    @Override
    public int deleteQtActivityByActivityId(Long activityId)
    {
        return qtActivityMapper.deleteQtActivityByActivityId(activityId);
    }
}
