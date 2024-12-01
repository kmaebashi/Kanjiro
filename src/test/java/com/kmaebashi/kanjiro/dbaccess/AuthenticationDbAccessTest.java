package com.kmaebashi.kanjiro.dbaccess;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.dto.DeviceDto;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationDbAccessTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        AuthenticationDbAccessTest.conn = KanjiroTestUtil.getConnection();
        AuthenticationDbAccessTest.logger = new FileLogger("./log", "BlogPostDbAccessTest");
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

    private static DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    @Test
    void upsertDeviceTest001() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        LocalDateTime lastLogin = LocalDateTime.of(2024, 11, 30, 14, 26, 00);
        int count = AuthenticationDbAccess.upsertDevice(invoker, "upsertDeviceTest001___", lastLogin,
                                                        "upsertDeviceTest001Secret_______");
        DeviceDto dto = AuthenticationDbAccess.getDevice(invoker, "upsertDeviceTest001___");
        assertEquals("upsertDeviceTest001___", dto.deviceId);
        assertEquals("20241130142600", localDateTimeFormatter.format(dto.lastLogin));
        assertEquals("upsertDeviceTest001Secret_______", dto.secretKey);

        LocalDateTime lastLogin2 = LocalDateTime.of(2024, 11, 30, 15, 26, 00);
        int count2 = AuthenticationDbAccess.upsertDevice(invoker, "upsertDeviceTest001___", lastLogin2,
                                                        "upsertDeviceTest001Secret2______");
        DeviceDto dto2 = AuthenticationDbAccess.getDevice(invoker, "upsertDeviceTest001___");
        assertEquals("upsertDeviceTest001___", dto2.deviceId);
        assertEquals("20241130152600", localDateTimeFormatter.format(dto2.lastLogin));
        assertEquals("upsertDeviceTest001Secret2______", dto2.secretKey);
    }
}