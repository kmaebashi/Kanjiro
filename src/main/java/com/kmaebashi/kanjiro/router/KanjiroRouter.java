package com.kmaebashi.kanjiro.router;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.kmaebashi.kanjiro.common.SessionKey;
import com.kmaebashi.kanjiro.controller.AuthenticateController;
import com.kmaebashi.kanjiro.controller.TopPageController;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.ServletContext;
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
        RoutingResult result = null;
        HttpSession session = request.getSession(true);
        String deviceId = (String)session.getAttribute(SessionKey.DEVICE_ID);

        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select(path, params);
        if (route == Route.NO_ROUTE) {
            throw new BadRequestException("URLが不正です。");
        }
        if (request.getMethod().equals("GET")) {
            if (deviceId == null
                && route == Route.TOP) {
                result = AuthenticateController.authenticateDevice(invoker, session, path);
                return result;
            } else if (route == Route.TOP) {
                result = TopPageController.showPage(invoker);
                return result;
            }
        } else if (request.getMethod().equals("POST")) {

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
