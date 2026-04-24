package com.ruoyi.qt.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qt.mapper.QtBookMapper;
import com.ruoyi.qt.domain.QtBook;
import com.ruoyi.qt.service.IQtBookService;

/**
 * 实验室图书Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@Service
public class QtBookServiceImpl implements IQtBookService 
{
    @Autowired
    private QtBookMapper qtBookMapper;

    /**
     * 查询实验室图书
     * 
     * @param bookId 实验室图书主键
     * @return 实验室图书
     */
    @Override
    public QtBook selectQtBookByBookId(Long bookId)
    {
        return qtBookMapper.selectQtBookByBookId(bookId);
    }

    /**
     * 查询实验室图书列表
     * 
     * @param qtBook 实验室图书
     * @return 实验室图书
     */
    @Override
    public List<QtBook> selectQtBookList(QtBook qtBook)
    {
        return qtBookMapper.selectQtBookList(qtBook);
    }

    /**
     * 新增实验室图书
     * 
     * @param qtBook 实验室图书
     * @return 结果
     */
    @Override
    public int insertQtBook(QtBook qtBook)
    {
        qtBook.setCreateTime(DateUtils.getNowDate());
        return qtBookMapper.insertQtBook(qtBook);
    }

    /**
     * 修改实验室图书
     * 
     * @param qtBook 实验室图书
     * @return 结果
     */
    @Override
    public int updateQtBook(QtBook qtBook)
    {
        qtBook.setUpdateTime(DateUtils.getNowDate());
        return qtBookMapper.updateQtBook(qtBook);
    }

    /**
     * 批量删除实验室图书
     * 
     * @param bookIds 需要删除的实验室图书主键
     * @return 结果
     */
    @Override
    public int deleteQtBookByBookIds(Long[] bookIds)
    {
        return qtBookMapper.deleteQtBookByBookIds(bookIds);
    }

    /**
     * 删除实验室图书信息
     * 
     * @param bookId 实验室图书主键
     * @return 结果
     */
    @Override
    public int deleteQtBookByBookId(Long bookId)
    {
        return qtBookMapper.deleteQtBookByBookId(bookId);
    }
}
