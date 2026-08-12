package com.ruoyi.qt.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.qt.domain.QtClothingOrder;
import com.ruoyi.qt.service.IQtClothingOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 实验室服装订单Controller
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/system/order")
public class QtClothingOrderController extends BaseController
{
    @Autowired
    private IQtClothingOrderService qtClothingOrderService;

    /**
     * 查询实验室服装订单列表
     */
    // @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(QtClothingOrder qtClothingOrder)
    {
        startPage();
        List<QtClothingOrder> list = qtClothingOrderService.selectQtClothingOrderList(qtClothingOrder);
        return getDataTable(list);
    }

    /**
     * 查询实验室服装订单详情列表
     */
    // @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/detailList")
    public TableDataInfo detailList(QtClothingOrder qtClothingOrder)
    {
        startPage();
        List<QtClothingOrder> list = qtClothingOrderService.selectQtClothingOrderDetailList(qtClothingOrder);
        return getDataTable(list);
    }

    /**
     * 导出实验室服装订单列表
     */
    // @PreAuthorize("@ss.hasPermi('system:order:export')")
    @Log(title = "实验室服装订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtClothingOrder qtClothingOrder)
    {
        List<QtClothingOrder> list = qtClothingOrderService.selectQtClothingOrderList(qtClothingOrder);
        ExcelUtil<QtClothingOrder> util = new ExcelUtil<QtClothingOrder>(QtClothingOrder.class);
        util.exportExcel(response, list, "实验室服装订单数据");
    }

    /**
     * 获取实验室服装订单详细信息
     */
    // @PreAuthorize("@ss.hasPermi('system:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(qtClothingOrderService.selectQtClothingOrderByOrderId(orderId));
    }

    /**
     * 新增实验室服装订单
     */
    // @PreAuthorize("@ss.hasPermi('system:order:add')")
    @Log(title = "实验室服装订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtClothingOrder qtClothingOrder)
    {
        return toAjax(qtClothingOrderService.insertQtClothingOrder(qtClothingOrder));
    }

    /**
     * 修改实验室服装订单
     */
    // @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "实验室服装订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtClothingOrder qtClothingOrder)
    {
        return toAjax(qtClothingOrderService.updateQtClothingOrder(qtClothingOrder));
    }

    /**
     * 删除实验室服装订单
     */
    // @PreAuthorize("@ss.hasPermi('system:order:remove')")
    @Log(title = "实验室服装订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(qtClothingOrderService.deleteQtClothingOrderByOrderIds(orderIds));
    }
}
