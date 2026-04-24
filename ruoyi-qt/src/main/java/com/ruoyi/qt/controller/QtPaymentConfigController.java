package com.ruoyi.system.controller;

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
import com.ruoyi.system.domain.QtPaymentConfig;
import com.ruoyi.system.service.IQtPaymentConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 固定付款码配置Controller
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/system/config")
public class QtPaymentConfigController extends BaseController
{
    @Autowired
    private IQtPaymentConfigService qtPaymentConfigService;

    /**
     * 查询固定付款码配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(QtPaymentConfig qtPaymentConfig)
    {
        startPage();
        List<QtPaymentConfig> list = qtPaymentConfigService.selectQtPaymentConfigList(qtPaymentConfig);
        return getDataTable(list);
    }

    /**
     * 导出固定付款码配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:config:export')")
    @Log(title = "固定付款码配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtPaymentConfig qtPaymentConfig)
    {
        List<QtPaymentConfig> list = qtPaymentConfigService.selectQtPaymentConfigList(qtPaymentConfig);
        ExcelUtil<QtPaymentConfig> util = new ExcelUtil<QtPaymentConfig>(QtPaymentConfig.class);
        util.exportExcel(response, list, "固定付款码配置数据");
    }

    /**
     * 获取固定付款码配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(qtPaymentConfigService.selectQtPaymentConfigByConfigId(configId));
    }

    /**
     * 新增固定付款码配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    @Log(title = "固定付款码配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtPaymentConfig qtPaymentConfig)
    {
        return toAjax(qtPaymentConfigService.insertQtPaymentConfig(qtPaymentConfig));
    }

    /**
     * 修改固定付款码配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    @Log(title = "固定付款码配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtPaymentConfig qtPaymentConfig)
    {
        return toAjax(qtPaymentConfigService.updateQtPaymentConfig(qtPaymentConfig));
    }

    /**
     * 删除固定付款码配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "固定付款码配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(qtPaymentConfigService.deleteQtPaymentConfigByConfigIds(configIds));
    }
}
