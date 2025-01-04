package com.kmaebashi.kanjiro.servlet;
import com.kmaebashi.kanjiro.router.KanjiroRouter;
import com.kmaebashi.kanjiro.util.Log;
import com.kmaebashi.nctfw.InternalException;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

public class KanjiroServlet extends HttpServlet {
    private KanjiroRouter router;
    private Logger logger;
    public void init() {
        ResourceBundle rb = ResourceBundle.getBundle("application");
        String logDirectory = rb.getString("kanjiro.log-directory");
        try {
            this.logger = new FileLogger(logDirectory, "KanjiroLog");
        } catch (IOException ex) {
            throw new InternalException("ログファイルの作成に失敗しました。", ex);
        }
        Log.setLogger(logger);
        this.router = new KanjiroRouter(this.getServletContext(), this.logger, rb);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Servlet.serice start." + request.getRequestURI());

        this.router.execute(request, response);

        logger.info("Servlet.serice end." + request.getRequestURI());
    }
}
