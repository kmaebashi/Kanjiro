package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.kanjiro.common.CookieKey;
import com.kmaebashi.kanjiro.common.SessionKey;
import com.kmaebashi.kanjiro.service.AuthenticateResult;
import com.kmaebashi.kanjiro.service.AuthenticationService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

public class AuthenticateController {
    private AuthenticateController() {}

    public static RoutingResult authenticateDevice(ControllerInvoker invoker, HttpSession session, String path) {
        return invoker.invoke((context) -> {
            Cookie authCookie = Util.searchCookie(context.getServletRequest(), CookieKey.AUTH_COOKIE);

            AuthenticateResult ar = AuthenticationService.authenticateDevice(context.getServiceInvoker(),
                    (authCookie != null ? authCookie.getValue() : null));
            session.setAttribute(SessionKey.DEVICE_ID, ar.deviceId);
            Cookie newCookie = new Cookie(CookieKey.AUTH_COOKIE, ar.authCookie);
            newCookie.setMaxAge(60 * 60 * 24 * 365 * 10);
            context.getServletResponse().addCookie(newCookie);

            RedirectResult ret = new RedirectResult(context.getServletRequest().getContextPath() + "/" + path);

            return ret;
        });
    }
}

