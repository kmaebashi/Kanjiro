package com.kmaebashi.kanjiro.router;

import com.kmaebashi.kanjiro.util.Log;
import com.kmaebashi.nctfw.BadRequestException;

import java.util.HashMap;

public class SelectRoute {
    private SelectRoute() {}

    static Route select(String path, HashMap<String, Object> params) {
        try {
            Log.info("path..[" + path + "]");
            if (path.endsWith("/")) {
                return Route.REDIRECT_REMOVE_SLASH;
            } else if (path.equals("")) {
                Log.info("Route.TOP selected");
                return Route.TOP;
            } else if (path.equals("event")) {
                return Route.EDIT_EVENT;
            } else if (path.equals("guest")) {
                return Route.GUEST;
            } else if (path.equals("linkdevice")) {
                return Route.LINK_DEVICE;
            } else if (path.equals("api/createeventinfo")) {
                return Route.CREATE_EVENT_INFO;
            } else if (path.equals("api/modifyeventinfo")) {
                return Route.MODIFY_EVENT_INFO;
            } else if (path.equals("api/postanswerinfo")) {
                return Route.POST_ANSWER_INFO;
            } else if (path.equals("api/postdevicepasscode")) {
                return Route.POST_DEVICE_PASSCODE;
            }
        } catch (Exception ex) {
            throw new BadRequestException("クエリストリングが不正です。");
        }
        return Route.NO_ROUTE;
    }
}
