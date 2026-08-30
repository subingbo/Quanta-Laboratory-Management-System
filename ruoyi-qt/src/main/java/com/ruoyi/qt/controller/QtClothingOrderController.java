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
import com.ruoyi.qt.domain.QtClothingOrder;
import com.ruoyi.qt.service.IQtClothingOrderService;
import com.ruoyi.qt.util.QtAuthUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.common.utils.StringUtils;

/**
 * 实验室服装订单Controller
 */
@RestController
@RequestMapping("/system/order")
public class QtClothingOrderController extends BaseController
{
    @Autowired
    private IQtClothingOrderService qtClothingOrderService;

    @Autowired
    private ServerConfig serverConfig;

    @GetMapping("/list")
    public TableDataInfo list(QtClothingOrder qtClothingOrder)
    {
        QtAuthUtils.restrictToSelfIfNoAdmin(QtAuthUtils.PERM_ORDER_LIST, qtClothingOrder::setUserId);
        startPage();
        List<QtClothingOrder> list = qtClothingOrderService.selectQtClothingOrderList(qtClothingOrder);
        fillOrderAliases(list);
        return getDataTable(list);
    }

    @GetMapping("/detailList")
    public TableDataInfo detailList(QtClothingOrder qtClothingOrder)
    {
        QtAuthUtils.restrictToSelfIfNoAdmin(QtAuthUtils.PERM_ORDER_LIST, qtClothingOrder::setUserId);
        startPage();
        List<QtClothingOrder> list = qtClothingOrderService.selectQtClothingOrderDetailList(qtClothingOrder);
        fillOrderAliases(list);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:order:export')")
    @Log(title = "实验室服装订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtClothingOrder qtClothingOrder)
    {
        List<QtClothingOrder> list = qtClothingOrderService.selectQtClothingOrderList(qtClothingOrder);
        ExcelUtil<QtClothingOrder> util = new ExcelUtil<QtClothingOrder>(QtClothingOrder.class);
        util.exportExcel(response, list, "实验室服装订单数据");
    }

    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        QtClothingOrder order = qtClothingOrderService.selectQtClothingOrderByOrderId(orderId);
        if (order != null)
        {
            fillOrderAliases(java.util.Collections.singletonList(order));
            if (!QtAuthUtils.hasAdminList(QtAuthUtils.PERM_ORDER_LIST) && !getUserId().equals(order.getUserId()))
            {
                return error("无权查看该订单");
            }
        }
        return success(order);
    }

    @Log(title = "实验室服装订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtClothingOrder qtClothingOrder)
    {
        if (qtClothingOrder.getUserId() == null || !QtAuthUtils.hasAdminList(QtAuthUtils.PERM_ORDER_LIST))
        {
            qtClothingOrder.setUserId(getUserId());
        }
        qtClothingOrder.setCreateBy(getUsername());
        return toAjax(qtClothingOrderService.insertQtClothingOrder(qtClothingOrder));
    }

    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "实验室服装订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtClothingOrder qtClothingOrder)
    {
        return toAjax(qtClothingOrderService.updateQtClothingOrder(qtClothingOrder));
    }

    @PreAuthorize("@ss.hasPermi('system:order:approve')")
    @Log(title = "塔服确认收款", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderId}/approve")
    public AjaxResult approve(@PathVariable Long orderId)
    {
        return toAjax(qtClothingOrderService.approveOrder(orderId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('system:order:remove')")
    @Log(title = "实验室服装订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(qtClothingOrderService.deleteQtClothingOrderByOrderIds(orderIds));
    }

    private void fillOrderAliases(List<QtClothingOrder> list)
    {
        if (list == null)
        {
            return;
        }
        for (QtClothingOrder order : list)
        {
            order.setOrderTime(order.getCreateTime());
            if (StringUtils.isNotEmpty(order.getPaymentProofPath()))
            {
                String path = order.getPaymentProofPath();
                if (path.startsWith("http://") || path.startsWith("https://"))
                {
                    order.setPaymentProofUrl(path);
                }
                else
                {
                    order.setPaymentProofUrl(serverConfig.getUrl() + path);
                }
            }
        }
    }
}
