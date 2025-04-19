package com.kmaebashi.kanjiro.service;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dto.UserDto;
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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DbUtilTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        DbUtilTest.conn = KanjiroTestUtil.getConnection();
        DbUtilTest.logger = new FileLogger("./log", "DbUtilTest");
        Log.setLogger(logger);
        deleteAll();
    }

    private static void deleteAll() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        KanjiroTestUtil.deleteAll(context, "USERS");
        KanjiroTestUtil.deleteAll(context, "DEVICES");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void getOrCreateUserTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);

        AuthenticationDbAccess.upsertDevice(invoker, "gOCreateUserTest001Dev", LocalDateTime.now(),
                                            "getOrCreateUserTest001DeviceId__");
        String userId  = DbUtil.getOrCreateUser(sc, "gOCreateUserTest001Dev", "gOCreateUserTest01User1");
        String userId2 = DbUtil.getOrCreateUser(sc, "gOCreateUserTest001Dev", "gOCreateUserTest01User2");
        assertEquals(userId, userId2);
        UserDto userDto = AuthenticationDbAccess.getUserByDeviceId(invoker, "gOCreateUserTest001Dev");
        assertEquals("gOCreateUserTest01User2", userDto.name);
    }

    @Test
    void getOrCreateUserTest002() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);

        AuthenticationDbAccess.upsertDevice(invoker, "gOCreateUserTest002Dev", LocalDateTime.now(),
                                            "getOrCreateUserTest002DeviceId__");
        String userId  = DbUtil.getOrCreateUser(sc, "gOCreateUserTest002Dev", "gOCreateUserTest01User1");
        String userId2 = DbUtil.getOrCreateUser(sc, "gOCreateUserTest002Dev", "gOCreateUserTest01User1");
        assertEquals(userId, userId2);
        UserDto userDto = AuthenticationDbAccess.getUserByDeviceId(invoker, "gOCreateUserTest002Dev");
        assertEquals("gOCreateUserTest01User1", userDto.name);
    }
}