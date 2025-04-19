package com.kmaebashi.kanjiro.service;

import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.util.Log;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class CsrfTokenServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        CsrfTokenServiceTest.conn = KanjiroTestUtil.getConnection();
        CsrfTokenServiceTest.logger = new FileLogger("./log", "CsrfTokenServiceTest");
        Log.setLogger(logger);
        deleteAll();
    }

    private static void deleteAll() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);

        KanjiroTestUtil.deleteAll(context, "CSRF_TOKENS");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void getNextCsrfTokenTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        String csrfToken = CsrfTokenService.getNextCsrfToken(si, "getNextCsrfTokenTest01");
        assertEquals(32, csrfToken.length());
        String dbToken = CsrfTokenService.getLastCsrfToken(si, "getNextCsrfTokenTest01");
        assertEquals(csrfToken, dbToken);
    }
}