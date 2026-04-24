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
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qt.domain.QtPaymentConfig;
import com.ruoyi.qt.service.IQtPaymentConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.framework.config.ServerConfig;

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
    @Autowired
    private ServerConfig serverConfig;

    /**
     * 查询固定付款码配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(QtPaymentConfig qtPaymentConfig)
    {
        startPage();
        List<QtPaymentConfig> list = qtPaymentConfigService.selectQtPaymentConfigList(qtPaymentConfig);
        fillImageUrl(list);
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
        QtPaymentConfig config = qtPaymentConfigService.selectQtPaymentConfigByConfigId(configId);
        fillImageUrl(config);
        return success(config);
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

    /**
     * 清空付款码图片（逻辑删除）
     */
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    @Log(title = "固定付款码配置", businessType = BusinessType.UPDATE)
    @DeleteMapping("/image/{configId}")
    public AjaxResult removeImage(@PathVariable Long configId)
    {
        QtPaymentConfig config = new QtPaymentConfig();
        config.setConfigId(configId);
        config.setQrImagePath("");
        config.setUpdateBy(getUsername());
        return toAjax(qtPaymentConfigService.updateQtPaymentConfig(config));
    }

    private void fillImageUrl(List<QtPaymentConfig> list)
    {
        for (QtPaymentConfig config : list)
        {
            fillImageUrl(config);
        }
    }

    private void fillImageUrl(QtPaymentConfig config)
    {
        if (config == null || StringUtils.isEmpty(config.getQrImagePath()))
        {
            return;
        }
        String imagePath = config.getQrImagePath();
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://"))
        {
            config.setQrImageUrl(imagePath);
            return;
        }
        if (imagePath.startsWith(RuoYiConfig.getProfile()))
        {
            imagePath = imagePath.substring(RuoYiConfig.getProfile().length());
        }
        if (!imagePath.startsWith("/"))
        {
            imagePath = "/" + imagePath;
        }
        config.setQrImageUrl(serverConfig.getUrl() + "/profile" + imagePath);
    }
}
