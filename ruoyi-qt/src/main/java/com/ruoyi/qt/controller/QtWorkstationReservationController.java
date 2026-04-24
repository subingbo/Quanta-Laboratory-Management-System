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
import com.ruoyi.qt.domain.QtWorkstationReservation;
import com.ruoyi.qt.service.IQtWorkstationReservationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 工位预约记录Controller
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/system/reservation")
public class QtWorkstationReservationController extends BaseController
{
    @Autowired
    private IQtWorkstationReservationService qtWorkstationReservationService;

    /**
     * 查询工位预约记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:reservation:list')")
    @GetMapping("/list")
    public TableDataInfo list(QtWorkstationReservation qtWorkstationReservation)
    {
        startPage();
        List<QtWorkstationReservation> list = qtWorkstationReservationService.selectQtWorkstationReservationList(qtWorkstationReservation);
        return getDataTable(list);
    }

    /**
     * 查询工位预约记录详情列表
     */
    @PreAuthorize("@ss.hasPermi('system:reservation:list')")
    @GetMapping("/detailList")
    public TableDataInfo detailList(QtWorkstationReservation qtWorkstationReservation)
    {
        startPage();
        List<QtWorkstationReservation> list = qtWorkstationReservationService.selectQtWorkstationReservationDetailList(qtWorkstationReservation);
        return getDataTable(list);
    }

    /**
     * 导出工位预约记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:reservation:export')")
    @Log(title = "工位预约记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtWorkstationReservation qtWorkstationReservation)
    {
        List<QtWorkstationReservation> list = qtWorkstationReservationService.selectQtWorkstationReservationList(qtWorkstationReservation);
        ExcelUtil<QtWorkstationReservation> util = new ExcelUtil<QtWorkstationReservation>(QtWorkstationReservation.class);
        util.exportExcel(response, list, "工位预约记录数据");
    }

    /**
     * 获取工位预约记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:reservation:query')")
    @GetMapping(value = "/{reservationId}")
    public AjaxResult getInfo(@PathVariable("reservationId") Long reservationId)
    {
        return success(qtWorkstationReservationService.selectQtWorkstationReservationByReservationId(reservationId));
    }

    /**
     * 新增工位预约记录
     */
    @PreAuthorize("@ss.hasPermi('system:reservation:add')")
    @Log(title = "工位预约记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtWorkstationReservation qtWorkstationReservation)
    {
        return toAjax(qtWorkstationReservationService.insertQtWorkstationReservation(qtWorkstationReservation));
    }

    /**
     * 修改工位预约记录
     */
    @PreAuthorize("@ss.hasPermi('system:reservation:edit')")
    @Log(title = "工位预约记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtWorkstationReservation qtWorkstationReservation)
    {
        return toAjax(qtWorkstationReservationService.updateQtWorkstationReservation(qtWorkstationReservation));
    }

    /**
     * 删除工位预约记录
     */
    @PreAuthorize("@ss.hasPermi('system:reservation:remove')")
    @Log(title = "工位预约记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{reservationIds}")
    public AjaxResult remove(@PathVariable Long[] reservationIds)
    {
        return toAjax(qtWorkstationReservationService.deleteQtWorkstationReservationByReservationIds(reservationIds));
    }
}
