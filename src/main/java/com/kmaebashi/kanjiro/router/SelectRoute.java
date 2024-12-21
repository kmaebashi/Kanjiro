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
            } else if (path.equals("api/posteventinfo")) {
                return Route.POST_EVENT_INFO;
            }
        } catch (Exception ex) {
            throw new BadRequestException("クエリストリングが不正です。");
        }
        return Route.NO_ROUTE;
    }
}
