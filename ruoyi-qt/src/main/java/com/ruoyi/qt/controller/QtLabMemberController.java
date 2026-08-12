package com.ruoyi.qt.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.qt.domain.QtLabMember;
import com.ruoyi.qt.service.IQtLabMemberService;

/**
 * Lab member list for C-end contacts.
 */
@RestController
@RequestMapping("/qt/member")
public class QtLabMemberController extends BaseController
{
    @Autowired
    private IQtLabMemberService qtLabMemberService;

    /**
     * List quanta members.
     */
    @GetMapping("/list")
    public TableDataInfo list(QtLabMember query)
    {
        startPage();
        List<QtLabMember> list = qtLabMemberService.selectLabMemberList(query);
        return getDataTable(list);
    }
}
