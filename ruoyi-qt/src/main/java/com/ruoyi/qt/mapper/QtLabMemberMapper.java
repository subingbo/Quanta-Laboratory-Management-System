package com.ruoyi.qt.mapper;

import java.util.List;
import com.ruoyi.qt.domain.QtLabMember;

/**
 * Lab member list mapper.
 */
public interface QtLabMemberMapper
{
    /**
     * Query quanta members (is_quanta_member = 1).
     */
    List<QtLabMember> selectLabMemberList(QtLabMember query);
}
