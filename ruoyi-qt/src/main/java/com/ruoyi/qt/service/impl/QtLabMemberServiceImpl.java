package com.ruoyi.qt.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qt.domain.QtLabMember;
import com.ruoyi.qt.mapper.QtLabMemberMapper;
import com.ruoyi.qt.service.IQtLabMemberService;

/**
 * Lab member list service implementation.
 */
@Service
public class QtLabMemberServiceImpl implements IQtLabMemberService
{
    @Autowired
    private QtLabMemberMapper qtLabMemberMapper;

    @Override
    public List<QtLabMember> selectLabMemberList(QtLabMember query)
    {
        return qtLabMemberMapper.selectLabMemberList(query);
    }
}
