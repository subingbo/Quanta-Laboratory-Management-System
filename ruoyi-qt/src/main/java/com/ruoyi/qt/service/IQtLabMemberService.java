package com.ruoyi.qt.service;

import java.util.List;
import com.ruoyi.qt.domain.QtLabMember;

/**
 * Lab member list service.
 */
public interface IQtLabMemberService
{
    /**
     * Query lab member list.
     */
    List<QtLabMember> selectLabMemberList(QtLabMember query);
}
