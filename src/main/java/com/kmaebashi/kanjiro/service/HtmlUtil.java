package com.kmaebashi.kanjiro.service;

public class HtmlUtil {
    private HtmlUtil() {}

    static String escapeHtml(String src) {
        return src.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }

    static String escapeHtml2(String src) {
        String escaped = HtmlUtil.escapeHtml(src);
        String linkCreated = HtmlUtil.createLinkAnchor(escaped);
        return nl2Br(linkCreated);
    }

    static String nl2Br(String str) {
        str = str.replaceAll("\r\n", "<br>");
        str = str.replaceAll("\n", "<br>");

        return str;
    }

    public static String createLinkAnchor(String src) {
        return  src.replaceAll("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+",
                "<a href=\"$0\">$0</a>");
    }
}
