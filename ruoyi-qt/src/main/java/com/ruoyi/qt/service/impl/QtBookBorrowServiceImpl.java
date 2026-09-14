package com.ruoyi.qt.service.impl;

import java.util.List;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.qt.cache.QtQueryCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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

    @Autowired
    private QtQueryCache qtQueryCache;

    /**
     * 借还会影响图书可借数量，手动缓存的分页列表要一并失效。
     */
    private void evictBookCache()
    {
        qtQueryCache.evict(CacheConstants.CACHE_QT_BOOK_LIST);
    }

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
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_BOOK_DETAIL, allEntries = true)
    public int insertQtBookBorrow(QtBookBorrow qtBookBorrow)
    {
        qtBookBorrow.setCreateTime(DateUtils.getNowDate());
        int rows = qtBookBorrowMapper.insertQtBookBorrow(qtBookBorrow);
        // 借出会改变 qt_book.available_count，图书列表/详情缓存必须一起清
        evictBookCache();
        return rows;
    }

    /**
     * 修改图书借阅记录
     * 
     * @param qtBookBorrow 图书借阅记录
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.CACHE_QT_BOOK_DETAIL, allEntries = true)
    public int updateQtBookBorrow(QtBookBorrow qtBookBorrow)
    {
        qtBookBorrow.setUpdateTime(DateUtils.getNowDate());
        int rows = qtBookBorrowMapper.updateQtBookBorrow(qtBookBorrow);
        evictBookCache();
        return rows;
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
