package com.kmaebashi.kanjiro.util;
import com.kmaebashi.kanjiro.common.CookieKey;
import com.kmaebashi.kanjiro.controller.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.kanjiro.common.SessionKey;

public class CsrfUtil {
    public static void addCsrfToken(DocumentResult dr, String token) {
        Document doc = dr.getDocument();
        Element metaElem = doc.createElement("meta");
        metaElem.attr("name", SessionKey.CSRF_TOKEN);
        metaElem.attr("content", token);
        doc.head().appendChild(metaElem);
    }
}