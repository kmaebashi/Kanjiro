package com.kmaebashi.kanjiro.service;

import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.util.Log;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class OrganizerPageServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        OrganizerPageServiceTest.conn = KanjiroTestUtil.getConnection();
        OrganizerPageServiceTest.logger = new FileLogger("./log", "OrganizerPageServiceTest");
        Log.setLogger(logger);
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }
    @Test
    void showTopPageTest001() {
        // DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        // DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(null,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        // Cookieなしでの初回閲覧
        DocumentResult dr = OrganizerPageService.showTopPage(si);
        String html = dr.getDocument().html();
        Element mockCalendar = dr.getDocument().getElementById("calendar-table");
        assertNull(mockCalendar);
    }
}