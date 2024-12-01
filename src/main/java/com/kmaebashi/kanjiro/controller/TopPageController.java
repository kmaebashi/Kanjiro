package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.kanjiro.common.CookieKey;
import com.kmaebashi.kanjiro.common.SessionKey;
import com.kmaebashi.kanjiro.service.AuthenticateResult;
import com.kmaebashi.kanjiro.service.AuthenticationService;
import com.kmaebashi.kanjiro.service.OrganizerPageService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

public class TopPageController {
    private TopPageController() {}

    public static RoutingResult showPage(ControllerInvoker invoker) {
        return invoker.invoke((context) -> {
            return OrganizerPageService.showTopPage(context.getServiceInvoker());
        });
    }

}
