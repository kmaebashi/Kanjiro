package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonParser;
import com.kmaebashi.kanjiro.controller.data.EventInfo;
import com.kmaebashi.kanjiro.service.OrganizerPageService;
import com.kmaebashi.kanjiro.util.CsrfUtil;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpServletRequest;

public class OrganizerController {
    private OrganizerController() {}

    public static RoutingResult postEventInfo(ControllerInvoker invoker, String deviceId) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            if (!CsrfUtil.checkCsrfToken(request)) {
                throw new BadRequestException("CSRFトークン不正", true);
            }
            try (JsonParser jsonParser = JsonParser.newInstance(request.getReader())) {

                JsonElement elem = jsonParser.parse();
                EventInfo eventInfo = ClassMapper.toObject(elem, EventInfo.class);

                return OrganizerPageService.mergeEventInfo(context.getServiceInvoker(), deviceId,eventInfo);
            }
        });
    }
}
