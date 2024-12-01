package com.kmaebashi.kanjiro.dbaccess;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.kanjiro.dto.DeviceDto;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;

public class AuthenticationDbAccess {
    private AuthenticationDbAccess() {}

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



    public static int InsertUser(DbAccessInvoker invoker, String userId, String name) {
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
}
