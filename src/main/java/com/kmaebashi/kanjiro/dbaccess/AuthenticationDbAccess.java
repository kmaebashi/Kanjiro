package com.kmaebashi.kanjiro.dbaccess;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.kanjiro.dto.DeviceDto;
import com.kmaebashi.kanjiro.dto.UserDto;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;

public class AuthenticationDbAccess {
    private AuthenticationDbAccess() {
    }

    public static DeviceDto getDevice(DbAccessInvoker invoker, String deviceId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM DEVICES
                    WHERE DEVICE_ID = :DEVICE_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("DEVICE_ID", deviceId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            DeviceDto dto = ResultSetMapper.toDto(rs, DeviceDto.class);
            return dto;
        });
    }

    public static int upsertDevice(DbAccessInvoker invoker,
                                   String deviceId, LocalDateTime lastLogin, String secretKey) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO DEVICES (
                          DEVICE_ID,
                          LAST_LOGIN,
                          SECRET_KEY,
                          CREATED_AT,
                          UPDATED_AT
                        ) VALUES (
                          :DEVICE_ID,
                          :LAST_LOGIN,
                          :SECRET_KEY,
                          now(),
                          now()
                        ) ON CONFLICT (DEVICE_ID)
                        DO UPDATE SET
                          LAST_LOGIN = EXCLUDED.LAST_LOGIN,
                          SECRET_KEY = EXCLUDED.SECRET_KEY,
                          UPDATED_AT = EXCLUDED.UPDATED_AT
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("DEVICE_ID", deviceId);
            params.put("LAST_LOGIN", lastLogin);
            params.put("SECRET_KEY", secretKey);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int insertUser(DbAccessInvoker invoker, String userId, String name) {
        return invoker.invoke((context) -> {
            String sql = """
                        INSERT INTO USERS (
                          USER_ID,
                          NAME,
                          CREATED_AT,
                          UPDATED_AT
                        ) VALUES (
                          :USER_ID,
                          :NAME,
                          now(),
                          now()
                        )
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("USER_ID", userId);
            params.put("NAME", name);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int setUserToDevice(DbAccessInvoker invoker, String deviceId, String userId) {
        return invoker.invoke((context) -> {
            String sql = """
                      UPDATE DEVICES SET
                        USER_ID = :USER_ID
                      WHERE
                        DEVICE_ID = :DEVICE_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("USER_ID", userId);
            params.put("DEVICE_ID", deviceId);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });

    }

    public static String getUserIdByDeviceId(DbAccessInvoker invoker, String deviceId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT USER_ID
                    FROM DEVICES
                    WHERE
                      DEVICE_ID = :DEVICE_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("DEVICE_ID", deviceId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            if (!rs.next()) {
                return null;
            }
            return rs.getString("USER_ID");
        });
    }

    public static UserDto getUserByDeviceId(DbAccessInvoker invoker, String deviceId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      U.USER_ID,
                      U.NAME,
                      U.LOGIN_ID,
                      U.PASSWORD,
                      D.DEVICE_ID
                    FROM DEVICES D
                    INNER JOIN USERS U
                    ON D.USER_ID = U.USER_ID
                    WHERE
                      DEVICE_ID = :DEVICE_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("DEVICE_ID", deviceId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            UserDto userDto = ResultSetMapper.toDto(rs, UserDto.class);

            return userDto;
        });
    }

    public static int updateUserName(DbAccessInvoker invoker, String userId, String newName) {
        return invoker.invoke((context) -> {
            String sql = """
                      UPDATE USERS SET
                        NAME = :NEW_NAME
                      WHERE
                        USER_ID = :USER_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("USER_ID", userId);
            params.put("NEW_NAME", newName);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }
}
