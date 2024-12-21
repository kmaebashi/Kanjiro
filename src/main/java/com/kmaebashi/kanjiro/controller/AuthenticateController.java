package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.kanjiro.common.CookieKey;
import com.kmaebashi.kanjiro.common.SessionKey;
import com.kmaebashi.kanjiro.service.AuthenticateResult;
import com.kmaebashi.kanjiro.service.AuthenticationService;
import com.kmaebashi.kanjiro.util.RandomIdGenerator;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

public class AuthenticateController {
    private AuthenticateController() {}

    public static RoutingResult authenticateDevice(ControllerInvoker invoker, HttpSession session, String[] csrfTokenBuf) {
        return invoker.invoke((context) -> {
            Cookie authCookie = Util.searchCookie(context.getServletRequest(), CookieKey.AUTH_COOKIE);

            AuthenticateResult ar = AuthenticationService.authenticateDevice(context.getServiceInvoker(),
                    (authCookie != null ? authCookie.getValue() : null));
            session.setAttribute(SessionKey.DEVICE_ID, ar.deviceId);
            Cookie newAuthCookie = new Cookie(CookieKey.AUTH_COOKIE, ar.authCookie);
            newAuthCookie.setMaxAge(60 * 60 * 24 * 365 * 10);
            newAuthCookie.setHttpOnly(true);
            newAuthCookie.setSecure(true);
            context.getServletResponse().addCookie(newAuthCookie);

            String csrfToken = RandomIdGenerator.getRandomId();
            Cookie csrfCookie = new Cookie(CookieKey.CSRF_TOKEN, csrfToken);
            csrfCookie.setMaxAge(60 * 60 * 24 * 365 * 10);
            csrfCookie.setHttpOnly(true);
            csrfCookie.setSecure(true);
            context.getServletResponse().addCookie(csrfCookie);

            csrfTokenBuf[0] = csrfToken;

            return null; // 戻り値は使わない。常にnullを返す。
        });
    }
}

