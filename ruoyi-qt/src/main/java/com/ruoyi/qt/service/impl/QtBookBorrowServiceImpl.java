package com.ruoyi.qt.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qt.mapper.QtBookBorrowMapper;
import com.ruoyi.qt.domain.QtBookBorrow;
import com.ruoyi.qt.service.IQtBookBorrowService;

/**
 * 图书借阅记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@Service
public class QtBookBorrowServiceImpl implements IQtBookBorrowService 
{
    @Autowired
    private QtBookBorrowMapper qtBookBorrowMapper;

    /**
     * 查询图书借阅记录
     * 
     * @param borrowId 图书借阅记录主键
     * @return 图书借阅记录
     */
    @Override
    public QtBookBorrow selectQtBookBorrowByBorrowId(Long borrowId)
    {
        return qtBookBorrowMapper.selectQtBookBorrowByBorrowId(borrowId);
    }

    /**
     * 查询图书借阅记录列表
     * 
     * @param qtBookBorrow 图书借阅记录
     * @return 图书借阅记录
     */
    @Override
    public List<QtBookBorrow> selectQtBookBorrowList(QtBookBorrow qtBookBorrow)
    {
        return qtBookBorrowMapper.selectQtBookBorrowList(qtBookBorrow);
    }

    @Override
    public List<QtBookBorrow> selectQtBookBorrowDetailList(QtBookBorrow qtBookBorrow)
    {
        return qtBookBorrowMapper.selectQtBookBorrowDetailList(qtBookBorrow);
    }

    /**
     * 新增图书借阅记录
     * 
     * @param qtBookBorrow 图书借阅记录
     * @return 结果
     */
    @Override
    public int insertQtBookBorrow(QtBookBorrow qtBookBorrow)
    {
        qtBookBorrow.setCreateTime(DateUtils.getNowDate());
        return qtBookBorrowMapper.insertQtBookBorrow(qtBookBorrow);
    }

    /**
     * 修改图书借阅记录
     * 
     * @param qtBookBorrow 图书借阅记录
     * @return 结果
     */
    @Override
    public int updateQtBookBorrow(QtBookBorrow qtBookBorrow)
    {
        qtBookBorrow.setUpdateTime(DateUtils.getNowDate());
        return qtBookBorrowMapper.updateQtBookBorrow(qtBookBorrow);
    }

    /**
     * 批量删除图书借阅记录
     * 
     * @param borrowIds 需要删除的图书借阅记录主键
     * @return 结果
     */
    @Override
    public int deleteQtBookBorrowByBorrowIds(Long[] borrowIds)
    {
        return qtBookBorrowMapper.deleteQtBookBorrowByBorrowIds(borrowIds);
    }

    /**
     * 删除图书借阅记录信息
     * 
     * @param borrowId 图书借阅记录主键
     * @return 结果
     */
    @Override
    public int deleteQtBookBorrowByBorrowId(Long borrowId)
    {
        return qtBookBorrowMapper.deleteQtBookBorrowByBorrowId(borrowId);
    }
}
