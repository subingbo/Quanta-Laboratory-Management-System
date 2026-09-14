package com.ruoyi.qt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.qt.mapper.QtDashboardMapper;

@RestController
@RequestMapping("/dashboard")
public class QtDashboardController extends BaseController
{
    @Autowired
    private QtDashboardMapper qtDashboardMapper;

    @PreAuthorize("@ss.hasPermi('qt:dashboard:stats')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        java.util.Map<String, Object> data = new java.util.HashMap<String, Object>();
        data.put("members", qtDashboardMapper.countActiveMembers());
        data.put("resumesToday", qtDashboardMapper.countTodayResumes());
        data.put("pendingReservations", qtDashboardMapper.countPendingReservations());
        data.put("pendingPayments", qtDashboardMapper.countPendingPayments());
        return success(data);
    }
}
