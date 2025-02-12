package com.kmaebashi.kanjiro.controller;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonParser;
import com.kmaebashi.kanjiro.common.HeaderKey;
import com.kmaebashi.kanjiro.controller.data.AnswerInfo;
import com.kmaebashi.kanjiro.controller.data.DevicePasscodeInfo;
import com.kmaebashi.kanjiro.service.GuestPageService;
import com.kmaebashi.kanjiro.service.LinkDevicePageService;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpServletRequest;

public class LinkDevicePageController {
    private LinkDevicePageController() {}

    public static RoutingResult showPage(ControllerInvoker invoker, String deviceId,
                                         String nextCsrfToken) {
        return invoker.invoke((context) -> {
            String eventId = context.getServletRequest().getParameter("eventId");
            if (eventId == null) {
                throw new BadRequestException("不正なURLです。");
            }
            return LinkDevicePageService.showPage(context.getServiceInvoker(), eventId, deviceId, nextCsrfToken);
        });
    }

    public static RoutingResult postDevicePasscode(ControllerInvoker invoker, String deviceId,
                                                   String lastCsrfToken) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            String headerToken = request.getHeader(HeaderKey.CSRF_TOKEN);
            if (headerToken == null || !headerToken.equals(lastCsrfToken)) {
                throw new BadRequestException("CSRFトークン不正", true);
            }

            try (JsonParser jsonParser = JsonParser.newInstance(request.getReader())) {

                JsonElement elem = jsonParser.parse();
                DevicePasscodeInfo devicePasscodeInfo = ClassMapper.toObject(elem, DevicePasscodeInfo.class);

                return LinkDevicePageService.postDevicePasscodeInfo(context.getServiceInvoker(), deviceId, devicePasscodeInfo);
            }
        });

    }
}
