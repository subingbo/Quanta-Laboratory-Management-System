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
import com.ruoyi.system.domain.QtActivitySignup;
import com.ruoyi.system.service.IQtActivitySignupService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 活动报名Controller
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/system/signup")
public class QtActivitySignupController extends BaseController
{
    @Autowired
    private IQtActivitySignupService qtActivitySignupService;

    /**
     * 查询活动报名列表
     */
    @PreAuthorize("@ss.hasPermi('system:signup:list')")
    @GetMapping("/list")
    public TableDataInfo list(QtActivitySignup qtActivitySignup)
    {
        startPage();
        List<QtActivitySignup> list = qtActivitySignupService.selectQtActivitySignupList(qtActivitySignup);
        return getDataTable(list);
    }

    /**
     * 导出活动报名列表
     */
    @PreAuthorize("@ss.hasPermi('system:signup:export')")
    @Log(title = "活动报名", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtActivitySignup qtActivitySignup)
    {
        List<QtActivitySignup> list = qtActivitySignupService.selectQtActivitySignupList(qtActivitySignup);
        ExcelUtil<QtActivitySignup> util = new ExcelUtil<QtActivitySignup>(QtActivitySignup.class);
        util.exportExcel(response, list, "活动报名数据");
    }

    /**
     * 获取活动报名详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:signup:query')")
    @GetMapping(value = "/{signupId}")
    public AjaxResult getInfo(@PathVariable("signupId") Long signupId)
    {
        return success(qtActivitySignupService.selectQtActivitySignupBySignupId(signupId));
    }

    /**
     * 新增活动报名
     */
    @PreAuthorize("@ss.hasPermi('system:signup:add')")
    @Log(title = "活动报名", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtActivitySignup qtActivitySignup)
    {
        return toAjax(qtActivitySignupService.insertQtActivitySignup(qtActivitySignup));
    }

    /**
     * 修改活动报名
     */
    @PreAuthorize("@ss.hasPermi('system:signup:edit')")
    @Log(title = "活动报名", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtActivitySignup qtActivitySignup)
    {
        return toAjax(qtActivitySignupService.updateQtActivitySignup(qtActivitySignup));
    }

    /**
     * 删除活动报名
     */
    @PreAuthorize("@ss.hasPermi('system:signup:remove')")
    @Log(title = "活动报名", businessType = BusinessType.DELETE)
	@DeleteMapping("/{signupIds}")
    public AjaxResult remove(@PathVariable Long[] signupIds)
    {
        return toAjax(qtActivitySignupService.deleteQtActivitySignupBySignupIds(signupIds));
    }
}
