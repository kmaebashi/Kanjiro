package com.kmaebashi.kanjiro.dbaccess;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.dto.EventDto;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class EventDbAccessTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        EventDbAccessTest.conn = KanjiroTestUtil.getConnection();
        EventDbAccessTest.logger = new FileLogger("./log", "EventDbAccessTest");
        deleteAll();
    }

    private static void deleteAll() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        invoker.invoke((localContext) -> {
            String sql = "DELETE FROM EVENTS";

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void insertEventTest001() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        EventDbAccess.insertEvent(invoker, "INSERTTEST001", "幹事さん", "ORGANIZER001", "なんとか会",
                "たのしみましょう", true, false);

        EventDto dto = EventDbAccess.getEvent(invoker, "INSERTTEST001");

        assertEquals("ORGANIZER001", dto.organizierId.trim());
        assertEquals("なんとか会", dto.eventName.trim());
        assertEquals("たのしみましょう", dto.description);
        assertEquals(true, dto.isSecretMode);
        assertEquals(false, dto.isAutoSchedule);
    }
}