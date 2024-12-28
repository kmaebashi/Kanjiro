package com.kmaebashi.kanjiro.router;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.kmaebashi.kanjiro.common.CookieKey;
import com.kmaebashi.kanjiro.common.SessionKey;
import com.kmaebashi.kanjiro.controller.AuthenticateController;
import com.kmaebashi.kanjiro.controller.EditEventController;
import com.kmaebashi.kanjiro.controller.GuestPageController;
import com.kmaebashi.kanjiro.controller.OrganizerController;
import com.kmaebashi.kanjiro.controller.TopPageController;
import com.kmaebashi.kanjiro.controller.Util;
import com.kmaebashi.kanjiro.util.RandomIdGenerator;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import com.kmaebashi.nctfw.Router;
import com.kmaebashi.simplelogger.Logger;
import jakarta.servlet.http.HttpSession;

public class KanjiroRouter extends Router {
    private ServletContext servletContext;
    private Logger logger;
    private ResourceBundle resourceBundle;

    public KanjiroRouter(ServletContext servletContext, Logger logger, ResourceBundle rb) {
        this.servletContext = servletContext;
        this.logger = logger;
        this.resourceBundle = rb;
    }

    public RoutingResult doRouting(String path, ControllerInvoker invoker, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String deviceId = (String)session.getAttribute(SessionKey.DEVICE_ID);
        String nextCsrfToken;
        if (deviceId == null) {
            String[] nextCsrfTokenBuf = new String[1];
            AuthenticateController.authenticateDevice(invoker, session, nextCsrfTokenBuf);
            nextCsrfToken = nextCsrfTokenBuf[0];
        } else {
            nextCsrfToken = Util.searchCookie(request, CookieKey.CSRF_TOKEN).getValue();
            if (nextCsrfToken == null) {
                nextCsrfToken = RandomIdGenerator.getRandomId();
            }
        }
        deviceId = (String)session.getAttribute(SessionKey.DEVICE_ID);

        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select(path, params);
        if (route == Route.NO_ROUTE) {
            throw new BadRequestException("URLが不正です。");
        }
        if (request.getMethod().equals("GET")) {
            if (route == Route.TOP) {
                return TopPageController.showPage(invoker, deviceId, nextCsrfToken);
            } else if (route == Route.EDIT_EVENT) {
                return EditEventController.showPage(invoker, deviceId, nextCsrfToken);
            } else if (route == Route.GUEST) {
                return GuestPageController.showPage(invoker, deviceId, nextCsrfToken);
            }
        } else if (request.getMethod().equals("POST")) {
            if (route == Route.POST_EVENT_INFO) {
                return OrganizerController.postEventInfo(invoker, deviceId);
            }
        }

        return null;
    }

    @Override
    public Connection getConnection() throws Exception {
        Context context = new InitialContext();
        DataSource ds = (DataSource)context.lookup("java:comp/env/jdbc/kanjiro");
        Connection conn = ds.getConnection();

        return  conn;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public Path getHtmlTemplateDirectory() {
        return Paths.get(this.servletContext.getRealPath("WEB-INF/htmltemplate"));
    }
}
