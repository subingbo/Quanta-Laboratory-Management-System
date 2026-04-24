package com.ruoyi.qt.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qt.mapper.QtClothingItemMapper;
import com.ruoyi.qt.domain.QtClothingItem;
import com.ruoyi.qt.service.IQtClothingItemService;

/**
 * 服装配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@Service
public class QtClothingItemServiceImpl implements IQtClothingItemService 
{
    @Autowired
    private QtClothingItemMapper qtClothingItemMapper;

    /**
     * 查询服装配置
     * 
     * @param itemId 服装配置主键
     * @return 服装配置
     */
    @Override
    public QtClothingItem selectQtClothingItemByItemId(Long itemId)
    {
        return qtClothingItemMapper.selectQtClothingItemByItemId(itemId);
    }

    /**
     * 查询服装配置列表
     * 
     * @param qtClothingItem 服装配置
     * @return 服装配置
     */
    @Override
    public List<QtClothingItem> selectQtClothingItemList(QtClothingItem qtClothingItem)
    {
        return qtClothingItemMapper.selectQtClothingItemList(qtClothingItem);
    }

    /**
     * 新增服装配置
     * 
     * @param qtClothingItem 服装配置
     * @return 结果
     */
    @Override
    public int insertQtClothingItem(QtClothingItem qtClothingItem)
    {
        qtClothingItem.setCreateTime(DateUtils.getNowDate());
        return qtClothingItemMapper.insertQtClothingItem(qtClothingItem);
    }

    /**
     * 修改服装配置
     * 
     * @param qtClothingItem 服装配置
     * @return 结果
     */
    @Override
    public int updateQtClothingItem(QtClothingItem qtClothingItem)
    {
        qtClothingItem.setUpdateTime(DateUtils.getNowDate());
        return qtClothingItemMapper.updateQtClothingItem(qtClothingItem);
    }

    /**
     * 批量删除服装配置
     * 
     * @param itemIds 需要删除的服装配置主键
     * @return 结果
     */
    @Override
    public int deleteQtClothingItemByItemIds(Long[] itemIds)
    {
        return qtClothingItemMapper.deleteQtClothingItemByItemIds(itemIds);
    }

    /**
     * 删除服装配置信息
     * 
     * @param itemId 服装配置主键
     * @return 结果
     */
    @Override
    public int deleteQtClothingItemByItemId(Long itemId)
    {
        return qtClothingItemMapper.deleteQtClothingItemByItemId(itemId);
    }
}
