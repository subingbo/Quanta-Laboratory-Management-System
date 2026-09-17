package com.ruoyi.qt.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.MailUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qt.util.QtDictUtils;
import com.ruoyi.system.service.ISysUserService;

/**
 * Send interview result mail to the applicant. Failures are logged only.
 */
@Component
public class QtInterviewNotifier
{
    private static final Logger log = LoggerFactory.getLogger(QtInterviewNotifier.class);

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private ISysUserService userService;

    public boolean notifyApplicant(Long userId, String subject, String content)
    {
        if (userId == null || StringUtils.isEmpty(subject) || StringUtils.isEmpty(content))
        {
            return false;
        }
        try
        {
            SysUser user = userService.selectUserById(userId);
            if (user == null || StringUtils.isEmpty(user.getEmail()))
            {
                log.warn("interview mail skipped, missing email, userId={}", userId);
                return false;
            }
            mailUtils.sendText(user.getEmail().trim(), subject, content);
            return true;
        }
        catch (Exception ex)
        {
            log.error("interview mail failed, userId={}", userId, ex);
            return false;
        }
    }

    public String departmentLabel(String code)
    {
        if (StringUtils.isEmpty(code))
        {
            return "";
        }
        String label = DictUtils.getDictLabel(QtDictUtils.DEPT, code);
        return StringUtils.isNotEmpty(label) ? label : code;
    }
}
