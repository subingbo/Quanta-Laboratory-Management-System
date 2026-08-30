package com.ruoyi.qt.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.qt.domain.QtBookBorrow;
import com.ruoyi.qt.service.IQtBookBorrowService;
import com.ruoyi.qt.util.QtAuthUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 图书借阅记录Controller
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/system/borrow")
public class QtBookBorrowController extends BaseController
{
    @Autowired
    private IQtBookBorrowService qtBookBorrowService;

    /**
     * 查询图书借阅记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(QtBookBorrow qtBookBorrow)
    {
        QtAuthUtils.restrictToSelfIfNoAdmin(QtAuthUtils.PERM_BORROW_LIST, qtBookBorrow::setUserId);
        startPage();
        List<QtBookBorrow> list = qtBookBorrowService.selectQtBookBorrowList(qtBookBorrow);
        return getDataTable(list);
    }

    /**
     * 查询图书借阅记录详情列表
     */
    @GetMapping("/detailList")
    public TableDataInfo detailList(QtBookBorrow qtBookBorrow)
    {
        QtAuthUtils.restrictToSelfIfNoAdmin(QtAuthUtils.PERM_BORROW_LIST, qtBookBorrow::setUserId);
        startPage();
        List<QtBookBorrow> list = qtBookBorrowService.selectQtBookBorrowDetailList(qtBookBorrow);
        return getDataTable(list);
    }

    /**
     * 导出图书借阅记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:borrow:export')")
    @Log(title = "图书借阅记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtBookBorrow qtBookBorrow)
    {
        List<QtBookBorrow> list = qtBookBorrowService.selectQtBookBorrowList(qtBookBorrow);
        ExcelUtil<QtBookBorrow> util = new ExcelUtil<QtBookBorrow>(QtBookBorrow.class);
        util.exportExcel(response, list, "图书借阅记录数据");
    }

    /**
     * 获取图书借阅记录详细信息
     */
    // @PreAuthorize("@ss.hasPermi('system:borrow:query')")
    @GetMapping(value = "/{borrowId}")
    public AjaxResult getInfo(@PathVariable("borrowId") Long borrowId)
    {
        return success(qtBookBorrowService.selectQtBookBorrowByBorrowId(borrowId));
    }

    /**
     * 新增图书借阅记录
     */
    @Log(title = "图书借阅记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtBookBorrow qtBookBorrow)
    {
        if (qtBookBorrow.getUserId() == null || !QtAuthUtils.hasAdminList(QtAuthUtils.PERM_BORROW_LIST))
        {
            qtBookBorrow.setUserId(getUserId());
        }
        return toAjax(qtBookBorrowService.insertQtBookBorrow(qtBookBorrow));
    }

    /**
     * 修改图书借阅记录
     */
    @PreAuthorize("@ss.hasPermi('system:borrow:edit')")
    @Log(title = "图书借阅记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtBookBorrow qtBookBorrow)
    {
        return toAjax(qtBookBorrowService.updateQtBookBorrow(qtBookBorrow));
    }

    /**
     * 删除图书借阅记录
     */
    @PreAuthorize("@ss.hasPermi('system:borrow:remove')")
    @Log(title = "图书借阅记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{borrowIds}")
    public AjaxResult remove(@PathVariable Long[] borrowIds)
    {
        return toAjax(qtBookBorrowService.deleteQtBookBorrowByBorrowIds(borrowIds));
    }
}
