package com.kmaebashi.kanjiro.service;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.controller.data.DevicePasscodeInfo;
import com.kmaebashi.kanjiro.controller.data.PostDevicePasscodeResult;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dbaccess.DevicePasscodeDbAccess;
import com.kmaebashi.kanjiro.dto.UserDto;
import com.kmaebashi.kanjiro.util.CsrfUtil;
import com.kmaebashi.kanjiro.util.RandomIdGenerator;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Path;

public class LinkDevicePageService {
    private LinkDevicePageService() {}

    public static DocumentResult showPage(ServiceInvoker invoker, String eventId, String deviceId,
                                          String nextCsrfToken) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("link_device.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            DocumentResult ret = new DocumentResult(doc);

            UserDto userDto = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(), deviceId);
            String passcode = null;
            if (userDto != null) {
                passcode = RandomIdGenerator.getRandomBase32();
                DevicePasscodeDbAccess.insertDevicePasscode(context.getDbAccessInvoker(), eventId, passcode, deviceId);
            }
            renderPage(doc, userDto, passcode);
            CsrfUtil.addCsrfToken(ret, nextCsrfToken);

            return ret;
        });
    }

    private static void renderPage(Document doc, UserDto userDto, String passcode) {
        if (userDto == null) {
            Element registeredElem = doc.getElementById("registered-description");
            registeredElem.remove();
        } else {
            Element notRegisteredElem = doc.getElementById("not-registered-description");
            notRegisteredElem.remove();
            Element userNameElem = doc.getElementById("user-name-span");
            userNameElem.text(userDto.name);
            Element passCodeSpanElem = doc.getElementById("passcode-span");
            passCodeSpanElem.text(passcode);
        }
    }

    public static JsonResult postDevicePasscodeInfo(ServiceInvoker invoker, String deviceId,
                                                    DevicePasscodeInfo devicePasscodeInfo) {
        return invoker.invoke((context) -> {
            PostDevicePasscodeResult result = new PostDevicePasscodeResult();

            String anotherDeviceId = DevicePasscodeDbAccess.getDeviceId(context.getDbAccessInvoker(),
                                                                        devicePasscodeInfo.eventId,
                                                                        devicePasscodeInfo.passcode);
            if (anotherDeviceId != null) {
                UserDto anotherUser = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(),
                                                                               anotherDeviceId);
                result.targetUserName = anotherUser.name;
                UserDto currentUser = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(),
                                                                               deviceId);
                if (currentUser != null) {
                    result.currentUserName = currentUser.name;
                    result.sameUser = (currentUser.userId == anotherUser.userId);
                }
                result.exists = true;
                AuthenticationDbAccess.linkDeviceToUser(context.getDbAccessInvoker(),
                                                        deviceId, anotherUser.userId);
            } else {
                result.exists = false;
            }
            String json = ClassMapper.toJson(result);
            return new JsonResult(json);
        });
    }
}
