package com.ruoyi.qt.util;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.EscapeUtil;

/**
 * Fixed HTML/plain templates for one recruitment result mail, keyed by offered department.
 */
public final class QtInterviewNoticeTemplates
{
    public static final String QR_CONTENT_ID = "qrcode";

    private QtInterviewNoticeTemplates()
    {
    }

    public static NoticeMail render(String realName, String offeredDepartment, boolean pass, boolean preview)
    {
        if (!pass)
        {
            return reject(realName);
        }
        return offer(realName, offeredDepartment, preview);
    }

    public static NoticeMail offer(String realName, String department, boolean preview)
    {
        String code = department == null ? "" : department.trim().toUpperCase();
        switch (code)
        {
            case "BACKEND":
                return offerMail(realName, "\u540e\u7aef\u90e8",
                        "\u606d\u559c\u4f60\u901a\u8fc7 Quanta \u540e\u7aef\u90e8\u9762\u8bd5\uff0c\u6b22\u8fce\u52a0\u5165\u540e\u7aef\u90e8\u3002\u6211\u4eec\u8d1f\u8d23\u670d\u52a1\u7aef\u3001\u63a5\u53e3\u4e0e\u5b9e\u9a8c\u5ba4\u7cfb\u7edf\u7684\u5f00\u53d1\u4e0e\u7ef4\u62a4\u3002",
                        "\u8bf7\u626b\u63cf\u4e0b\u65b9\u7fa4\u4e8c\u7ef4\u7801\u52a0\u5165\u540e\u7aef\u4ea4\u6d41\u7fa4\uff0c\u5e76\u7559\u610f\u540e\u7eed\u5b89\u6392\u3002",
                        preview);
            case "PRODUCT":
                return offerMail(realName, "\u4ea7\u54c1\u90e8",
                        "\u606d\u559c\u4f60\u901a\u8fc7 Quanta \u4ea7\u54c1\u90e8\u9762\u8bd5\uff0c\u6b22\u8fce\u52a0\u5165\u4ea7\u54c1\u90e8\u3002\u6211\u4eec\u8d1f\u8d23\u9700\u6c42\u68b3\u7406\u3001\u6d3b\u52a8\u7b56\u5212\u548c\u5b9e\u9a8c\u5ba4\u4ea7\u54c1\u63a8\u8fdb\u3002",
                        "\u8bf7\u626b\u63cf\u4e0b\u65b9\u7fa4\u4e8c\u7ef4\u7801\u52a0\u5165\u4ea7\u54c1\u90e8\u7fa4\uff0c\u5e76\u7559\u610f\u540e\u7eed\u5b89\u6392\u3002",
                        preview);
            case "DESIGN":
                return offerMail(realName, "\u8bbe\u8ba1\u90e8",
                        "\u606d\u559c\u4f60\u901a\u8fc7 Quanta \u8bbe\u8ba1\u90e8\u9762\u8bd5\uff0c\u6b22\u8fce\u52a0\u5165\u8bbe\u8ba1\u90e8\u3002\u6211\u4eec\u8d1f\u8d23\u89c6\u89c9\u3001\u7269\u6599\u548c\u5b9e\u9a8c\u5ba4\u76f8\u5173\u7684\u8bbe\u8ba1\u4ea7\u51fa\u3002",
                        "\u8bf7\u626b\u63cf\u4e0b\u65b9\u7fa4\u4e8c\u7ef4\u7801\u52a0\u5165\u8bbe\u8ba1\u90e8\u7fa4\uff0c\u5e76\u7559\u610f\u540e\u7eed\u5b89\u6392\u3002",
                        preview);
            case "FRONTEND":
                return offerMail(realName, "\u524d\u7aef\u90e8",
                        "\u606d\u559c\u4f60\u901a\u8fc7 Quanta \u524d\u7aef\u90e8\u9762\u8bd5\uff0c\u6b22\u8fce\u52a0\u5165\u524d\u7aef\u90e8\u3002\u6211\u4eec\u8d1f\u8d23\u9875\u9762\u3001\u4ea4\u4e92\u548c\u5b9e\u9a8c\u5ba4\u76f8\u5173\u7684\u524d\u7aef\u5b9e\u73b0\u3002",
                        "\u8bf7\u626b\u63cf\u4e0b\u65b9\u7fa4\u4e8c\u7ef4\u7801\u52a0\u5165\u524d\u7aef\u7fa4\uff0c\u5e76\u7559\u610f\u540e\u7eed\u5b89\u6392\u3002",
                        preview);
            case "ANDROID":
                return offerMail(realName, "\u5b89\u5353\u90e8",
                        "\u606d\u559c\u4f60\u901a\u8fc7 Quanta \u5b89\u5353\u90e8\u9762\u8bd5\uff0c\u6b22\u8fce\u52a0\u5165\u5b89\u5353\u90e8\u3002",
                        "\u8bf7\u626b\u63cf\u4e0b\u65b9\u7fa4\u4e8c\u7ef4\u7801\u52a0\u5165\u5b89\u5353\u7fa4\uff0c\u5e76\u7559\u610f\u540e\u7eed\u5b89\u6392\u3002",
                        preview);
            default:
                String label = StringUtils.isEmpty(code) ? "Quanta" : code;
                return offerMail(realName, label,
                        "\u606d\u559c\u4f60\u901a\u8fc7 Quanta " + label + "\u9762\u8bd5\uff0c\u6b22\u8fce\u52a0\u5165\u3002",
                        "\u8bf7\u626b\u63cf\u4e0b\u65b9\u7fa4\u4e8c\u7ef4\u7801\u52a0\u5165\u90e8\u95e8\u7fa4\uff0c\u5e76\u7559\u610f\u540e\u7eed\u5b89\u6392\u3002",
                        preview);
        }
    }

    public static NoticeMail reject(String realName)
    {
        String name = escape(realName);
        String greeting = "\u4f60\u597d " + name + "\uff0c";
        String body = "\u611f\u8c22\u4f60\u53c2\u52a0 Quanta \u62db\u65b0\u9762\u8bd5\u3002\u5f88\u9057\u61be\u672c\u6b21\u672a\u80fd\u5f55\u7528\uff0c\u795d\u4f60\u5b66\u4e60\u987a\u5229\u3002";
        return new NoticeMail("\u6dd8\u6c70\u901a\u77e5", greeting + "\n\n" + body,
                "<p>" + greeting + "</p><p>" + body + "</p>");
    }

    private static NoticeMail offerMail(String realName, String deptLabel, String intro, String scanHint,
            boolean preview)
    {
        String name = escape(realName);
        String greeting = "\u4f60\u597d " + name + "\uff0c";
        String fallback = "\u82e5\u56fe\u7247\u65e0\u6cd5\u663e\u793a\uff0c\u8bf7\u6253\u5f00\u9644\u4ef6\u4e2d\u7684\u4e8c\u7ef4\u7801\u3002";
        String qrBlock = preview
                ? "<p><em>\u53d1\u9001\u65f6\u5c06\u5728\u6b64\u5904\u663e\u793a\u7fa4\u4e8c\u7ef4\u7801</em></p>"
                : "<p><img src=\"cid:" + QR_CONTENT_ID
                        + "\" alt=\"\u90e8\u95e8\u7fa4\u4e8c\u7ef4\u7801\" style=\"max-width:240px;height:auto;\" /></p>"
                        + "<p>" + fallback + "</p>";
        String html = "<p>" + greeting + "</p><p>" + intro + "</p><p>" + scanHint + "</p>" + qrBlock;
        String plain = greeting + "\n\n" + intro + "\n\n" + scanHint + "\n\n"
                + (preview ? "\u53d1\u9001\u65f6\u5c06\u5728\u6b64\u5904\u663e\u793a\u7fa4\u4e8c\u7ef4\u7801"
                        : "\u8bf7\u626b\u63cf\u90ae\u4ef6\u4e2d\u7684\u90e8\u95e8\u7fa4\u4e8c\u7ef4\u7801\u3002" + fallback);
        return new NoticeMail("Quanta " + deptLabel + "\u5f55\u7528\u901a\u77e5", plain, html);
    }

    private static String escape(String value)
    {
        return EscapeUtil.escape(StringUtils.isEmpty(value) ? "" : value);
    }

    public static final class NoticeMail
    {
        public final String subject;
        public final String plain;
        public final String html;

        private NoticeMail(String subject, String plain, String html)
        {
            this.subject = subject;
            this.plain = plain;
            this.html = html;
        }
    }
}
