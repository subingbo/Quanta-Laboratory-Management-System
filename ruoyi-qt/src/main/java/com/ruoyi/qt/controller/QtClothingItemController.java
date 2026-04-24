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
import com.ruoyi.system.domain.QtClothingItem;
import com.ruoyi.system.service.IQtClothingItemService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 服装配置Controller
 * 
 * @author ruoyi
 * @date 2026-04-24
 */
@RestController
@RequestMapping("/system/item")
public class QtClothingItemController extends BaseController
{
    @Autowired
    private IQtClothingItemService qtClothingItemService;

    /**
     * 查询服装配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:item:list')")
    @GetMapping("/list")
    public TableDataInfo list(QtClothingItem qtClothingItem)
    {
        startPage();
        List<QtClothingItem> list = qtClothingItemService.selectQtClothingItemList(qtClothingItem);
        return getDataTable(list);
    }

    /**
     * 导出服装配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:item:export')")
    @Log(title = "服装配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QtClothingItem qtClothingItem)
    {
        List<QtClothingItem> list = qtClothingItemService.selectQtClothingItemList(qtClothingItem);
        ExcelUtil<QtClothingItem> util = new ExcelUtil<QtClothingItem>(QtClothingItem.class);
        util.exportExcel(response, list, "服装配置数据");
    }

    /**
     * 获取服装配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:item:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId)
    {
        return success(qtClothingItemService.selectQtClothingItemByItemId(itemId));
    }

    /**
     * 新增服装配置
     */
    @PreAuthorize("@ss.hasPermi('system:item:add')")
    @Log(title = "服装配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QtClothingItem qtClothingItem)
    {
        return toAjax(qtClothingItemService.insertQtClothingItem(qtClothingItem));
    }

    /**
     * 修改服装配置
     */
    @PreAuthorize("@ss.hasPermi('system:item:edit')")
    @Log(title = "服装配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QtClothingItem qtClothingItem)
    {
        return toAjax(qtClothingItemService.updateQtClothingItem(qtClothingItem));
    }

    /**
     * 删除服装配置
     */
    @PreAuthorize("@ss.hasPermi('system:item:remove')")
    @Log(title = "服装配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(qtClothingItemService.deleteQtClothingItemByItemIds(itemIds));
    }
}
