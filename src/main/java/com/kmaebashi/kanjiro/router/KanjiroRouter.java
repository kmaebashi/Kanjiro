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
import com.kmaebashi.kanjiro.controller.CsrfTokenController;
import com.kmaebashi.kanjiro.controller.EditEventController;
import com.kmaebashi.kanjiro.controller.GuestPageController;
import com.kmaebashi.kanjiro.controller.LinkDevicePageController;
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
        if (deviceId == null) {
            AuthenticateController.authenticateDevice(invoker, session);
        }
        deviceId = (String)session.getAttribute(SessionKey.DEVICE_ID);

        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select(path, params);
        if (route == Route.NO_ROUTE) {
            throw new BadRequestException("URLが不正です。");
        }
        if (request.getMethod().equals("GET")) {
            this.logger.info("GET path.." + path);
            String[] nextCsrfTokenBuf = new String[1];
            CsrfTokenController.getNextCsrfToken(invoker, deviceId, nextCsrfTokenBuf);
            String nextCsrfToken = nextCsrfTokenBuf[0];
            this.logger.info("nextCsrfToken.." + nextCsrfToken);
            if (route == Route.TOP) {
                return TopPageController.showPage(invoker, deviceId, nextCsrfToken);
            } else if (route == Route.EDIT_EVENT) {
                return EditEventController.showPage(invoker, deviceId, nextCsrfToken);
            } else if (route == Route.GUEST) {
                return GuestPageController.showPage(invoker, deviceId, nextCsrfToken);
            } else if (route == Route.LINK_DEVICE) {
                return LinkDevicePageController.showPage(invoker, deviceId, nextCsrfToken);
            }
        } else if (request.getMethod().equals("POST")) {
            this.logger.info("POST path.." + path);
            String[] lastCsrfTokenBuf = new String[1];
            CsrfTokenController.getLastCsrfToken(invoker, deviceId, lastCsrfTokenBuf);
            String lastCsrfToken = lastCsrfTokenBuf[0];
            this.logger.info("lastCsrfToken.." + lastCsrfToken);
            if (route == Route.CREATE_EVENT_INFO) {
                return OrganizerController.postEventInfo(invoker, deviceId, lastCsrfToken, true);
            } else if (route == Route.MODIFY_EVENT_INFO) {
                return OrganizerController.postEventInfo(invoker, deviceId, lastCsrfToken, false);
            } else if (route == Route.POST_ANSWER_INFO) {
                return GuestPageController.postAnswerInfo(invoker, deviceId, lastCsrfToken);
            } else if (route == Route.DELETE_ANSWER) {
                return GuestPageController.deleteAnswer(invoker, deviceId, lastCsrfToken);
            } else if (route == Route.POST_DEVICE_PASSCODE) {
                return LinkDevicePageController.postDevicePasscode(invoker, deviceId, lastCsrfToken);
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
