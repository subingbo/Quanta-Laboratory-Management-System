package com.ruoyi.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import jakarta.mail.internet.MimeMessage;

/**
 * SMTP sender for Aliyun DirectMail. Credentials come from env only.
 */
@Component
public class MailUtils
{
    private static final Logger log = LoggerFactory.getLogger(MailUtils.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendText(String to, String subject, String content)
    {
        if (mailSender == null || StringUtils.isEmpty(from))
        {
            throw new ServiceException("\u90ae\u4ef6\u670d\u52a1\u672a\u914d\u7f6e");
        }
        try
        {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            mailSender.send(message);
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            log.error("send mail failed, to={}", to, ex);
            throw new ServiceException("\u90ae\u4ef6\u53d1\u9001\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5");
        }
    }
}
