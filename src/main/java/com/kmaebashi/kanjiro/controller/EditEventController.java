package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.kanjiro.service.OrganizerPageService;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;

public class EditEventController {
    private EditEventController() {}

    public static RoutingResult showPage(ControllerInvoker invoker, String deviceId, String nextCsrfToken) {
        return invoker.invoke((context) -> {
            String eventId = context.getServletRequest().getParameter("eventId");
            if (eventId == null) {
                throw new BadRequestException("不正なURLです。");
            }
            String url = context.getServletRequest().getRequestURL().toString();
            url = url.replaceFirst("\\.do$", "");

            return OrganizerPageService.showEditEventPage(context.getServiceInvoker(), eventId, deviceId,
                                                          nextCsrfToken, url);
        });
    }
}
