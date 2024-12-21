package com.kmaebashi.kanjiro.service;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.controller.data.EventInfo;
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

    private static void deleteAll() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        invoker.invoke((localContext) -> {
            String sql = "DELETE FROM EVENTS";

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            int result = npps.getPreparedStatement().executeUpdate();

            String sql2 = "DELETE FROM EVENTS";

            NamedParameterPreparedStatement npps2
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql2);
            result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }
    @Test
    void showTopPageTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        // Cookieなしでの初回閲覧
        DocumentResult dr = OrganizerPageService.showTopPage(si, "deviceId", "CSRFTOKEN");
        String html = dr.getDocument().html();
        Element mockCalendar = dr.getDocument().getElementById("calendar-table");
        assertNull(mockCalendar);
    }

    @Test
    void createNewEventTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        EventInfo eventInfo = new EventInfo();
        eventInfo.organizerName = "Test001幹事";
        eventInfo.eventName = "Test001送別会";
        eventInfo.eventDescription = "Test001さんの送別会です。";
        eventInfo.scheduleArray = new String[] {
                "12/24(火) 17:00～",
                "12/25(水) 17:00～",
                "12/26(木) 17:00～"
        };
        eventInfo.isSecretMode = true;
        eventInfo.isAutoSchedule = true;
        eventInfo.registerForce = false;
        eventInfo.updatedAt = "20241221123315111";

        OrganizerPageService.createNewEvent(sc, "createNewEventTest001_", eventInfo);
    }
}