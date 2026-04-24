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
import com.ruoyi.qt.domain.QtActivity;
import com.ruoyi.qt.service.IQtActivityService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 实验室活动Controller
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/system/activity")
public class QtActivityController extends BaseController
{
    @Autowired
    private IQtActivityService qtActivityService;

    /**
     * 查询实验室活动列表
     */
    @PreAuthorize("@ss.hasPermi('system:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(QtActivity qtActivity)
    {
        startPage();
        List<QtActivity> list = qtActivityService.selectQtActivityList(qtActivity);
        return getDataTable(list);
    }

    /**
     * 导出实验室活动列表
     */
    @PreAuthorize("@ss.hasPermi('system:activity:export')")
    @Log(title = "实验室活动", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtActivity qtActivity)
    {
        List<QtActivity> list = qtActivityService.selectQtActivityList(qtActivity);
        ExcelUtil<QtActivity> util = new ExcelUtil<QtActivity>(QtActivity.class);
        util.exportExcel(response, list, "实验室活动数据");
    }

    /**
     * 获取实验室活动详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:activity:query')")
    @GetMapping(value = "/{activityId}")
    public AjaxResult getInfo(@PathVariable("activityId") Long activityId)
    {
        return success(qtActivityService.selectQtActivityByActivityId(activityId));
    }

    /**
     * 新增实验室活动
     */
    @PreAuthorize("@ss.hasPermi('system:activity:add')")
    @Log(title = "实验室活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtActivity qtActivity)
    {
        return toAjax(qtActivityService.insertQtActivity(qtActivity));
    }

    /**
     * 修改实验室活动
     */
    @PreAuthorize("@ss.hasPermi('system:activity:edit')")
    @Log(title = "实验室活动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtActivity qtActivity)
    {
        return toAjax(qtActivityService.updateQtActivity(qtActivity));
    }

    /**
     * 删除实验室活动
     */
    @PreAuthorize("@ss.hasPermi('system:activity:remove')")
    @Log(title = "实验室活动", businessType = BusinessType.DELETE)
	@DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        return toAjax(qtActivityService.deleteQtActivityByActivityIds(activityIds));
    }
}
