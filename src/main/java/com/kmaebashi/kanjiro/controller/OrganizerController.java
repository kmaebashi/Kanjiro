package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonParser;
import com.kmaebashi.kanjiro.common.HeaderKey;
import com.kmaebashi.kanjiro.controller.data.EventInfo;
import com.kmaebashi.kanjiro.controller.data.PostEventInfoResult;
import com.kmaebashi.kanjiro.service.OrganizerPageService;
import com.kmaebashi.kanjiro.util.CsrfUtil;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpServletRequest;

public class OrganizerController {
    private OrganizerController() {}

    public static RoutingResult postEventInfo(ControllerInvoker invoker, String deviceId, String lastCsrfToken,
                                              boolean createFlag) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            String headerToken = request.getHeader(HeaderKey.CSRF_TOKEN);
            if (headerToken == null || !headerToken.equals(lastCsrfToken)) {
                context.getLogger().info("headerToken.." + headerToken + ", lastCsrfToken.." + lastCsrfToken);
                throw new BadRequestException("CSRFトークン不正", true);
            }
            try (JsonParser jsonParser = JsonParser.newInstance(request.getReader())) {

                JsonElement elem = jsonParser.parse();
                EventInfo eventInfo = ClassMapper.toObject(elem, EventInfo.class);

                PostEventInfoResult result;
                if (createFlag) {
                    result = OrganizerPageService.createNewEvent(context.getServiceInvoker(), deviceId, eventInfo);
                } else {
                    result = OrganizerPageService.modifyEventInfo(context.getServiceInvoker(), deviceId, eventInfo);
                }
                String json = ClassMapper.toJson(result);

                return new JsonResult(json);
            }
        });
    }
}
