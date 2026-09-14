package com.ruoyi.qt.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QtAuthUtilsTest
{
    @AfterEach
    void clear()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void memberCannotReadOthersRecord()
    {
        login(8L, Set.of("qt:other:list"));
        assertThrows(ServiceException.class,
                () -> QtAuthUtils.assertOwnerOrAdmin(9L, QtAuthUtils.PERM_BORROW_LIST));
    }

    @Test
    void memberCanReadOwnRecord()
    {
        login(8L, Set.of("qt:other:list"));
        assertDoesNotThrow(() -> QtAuthUtils.assertOwnerOrAdmin(8L, QtAuthUtils.PERM_BORROW_LIST));
    }

    @Test
    void listAdminCanReadOthers()
    {
        login(8L, Set.of(QtAuthUtils.PERM_BORROW_LIST));
        assertDoesNotThrow(() -> QtAuthUtils.assertOwnerOrAdmin(9L, QtAuthUtils.PERM_BORROW_LIST));
    }

    @Test
    void restrictToSelfSetsUserIdForMember()
    {
        login(8L, Set.of("qt:other:list"));
        Long[] holder = new Long[1];
        QtAuthUtils.restrictToSelfIfNoAdmin(QtAuthUtils.PERM_BORROW_LIST, id -> holder[0] = id);
        org.junit.jupiter.api.Assertions.assertEquals(8L, holder[0]);
    }

    private void login(Long userId, Set<String> permissions)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        SysRole role = new SysRole();
        role.setRoleKey("qt_member");
        user.setRoles(List.of(role));
        LoginUser loginUser = new LoginUser(userId, 100L, user, new HashSet<>(permissions));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
