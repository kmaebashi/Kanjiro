package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.kanjiro.common.SessionKey;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpSession;
import com.kmaebashi.kanjiro.service.CsrfTokenService;

public class CsrfTokenController {
    private CsrfTokenController() {}

    public static RoutingResult getNextCsrfToken(ControllerInvoker invoker, String deviceId, String[] csrfTokenBuf) {
        return invoker.invoke((context) -> {
            HttpSession session = context.getServletRequest().getSession();
            String csrfToken = (String)session.getAttribute(SessionKey.CSRF_TOKEN);

            if (csrfToken == null) {
                csrfToken = CsrfTokenService.getNextCsrfToken(context.getServiceInvoker(), deviceId);
                session.setAttribute(SessionKey.CSRF_TOKEN, csrfToken);
            }
            csrfTokenBuf[0] = csrfToken;
            return null; // ControllerはRoutingResult以外返せないので、戻り値はダミー
        });
    }

    public static RoutingResult getLastCsrfToken(ControllerInvoker invoker, String deviceId, String[] csrfTokenBuf) {
        return invoker.invoke((context) -> {
            HttpSession session = context.getServletRequest().getSession();
            String csrfToken = (String)session.getAttribute(SessionKey.CSRF_TOKEN);

            if (csrfToken == null) {
                csrfToken = CsrfTokenService.getLastCsrfToken(context.getServiceInvoker(), deviceId);
                String newCsrfToken = CsrfTokenService.getNextCsrfToken(context.getServiceInvoker(), deviceId);
                session.setAttribute(SessionKey.CSRF_TOKEN, newCsrfToken);
            }
            csrfTokenBuf[0] = csrfToken;
            return null;
        });
    }
}
