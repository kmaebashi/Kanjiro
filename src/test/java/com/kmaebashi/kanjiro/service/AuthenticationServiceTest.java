package com.kmaebashi.kanjiro.service;

import java.nio.file.Paths;
import java.sql.Connection;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
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

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        AuthenticationServiceTest.conn = KanjiroTestUtil.getConnection();
        AuthenticationServiceTest.logger = new FileLogger("./log", "AuthenticationServiceTest");
        Log.setLogger(logger);
        deleteAll();
    }

    private static void deleteAll() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        invoker.invoke((localContext) -> {
            String sql = "DELETE FROM DEVICES";

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
    void authenticateDeviceTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        // Cookieなしでの初回閲覧
        AuthenticateResult ar1 = AuthenticationService.authenticateDevice(si, null);
        // 同じCookieで2回目閲覧
        AuthenticateResult ar2 = AuthenticationService.authenticateDevice(si, ar1.authCookie);
        assertEquals(ar1.deviceId, ar2.deviceId);
        // Cookieなしで3回目閲覧
        AuthenticateResult ar3 = AuthenticationService.authenticateDevice(si, null);
        assertNotEquals(ar2.deviceId, ar3.deviceId);
        // 不正なCookieで閲覧
        AuthenticateResult ar4 = AuthenticationService.authenticateDevice(si, "badcookie");
        assertNotEquals(ar3.deviceId, ar4.deviceId);
        // 異なるCookieで閲覧
        AuthenticateResult ar5 = AuthenticationService.authenticateDevice(si, "hel7B5lKQJ6tS_WnMvHjog:20241130150912:1AhEECDlrDtpjYo-4rBSnmJvp9D4UTT4hxsEucrpzjc");
        assertNotEquals(ar4.deviceId, ar5.deviceId);
        // 最終ログインのみ不正
        String[] splitted = ar5.authCookie.split(":");
        String badCookie = splitted[0] + ":" + "19000101000000" + ":" + splitted[2];
        AuthenticateResult ar6 = AuthenticationService.authenticateDevice(si, badCookie);
        assertNotEquals(ar5.deviceId, ar6.deviceId);
    }
}