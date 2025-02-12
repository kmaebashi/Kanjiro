package com.kmaebashi.kanjiro.dbaccess;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.kanjiro.dto.EventDto;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;

public class DevicePasscodeDbAccess {
    private DevicePasscodeDbAccess() {}

    public static int insertDevicePasscode(DbAccessInvoker invoker,
                                           String eventId, String passcode, String deviceId) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO DEVICE_PASSCODE (
                      EVENT_ID,
                      PASSCODE,
                      DEVICE_ID,
                      CREATED_AT
                    ) VALUES (
                      :EVENT_ID,
                      :PASSCODE,
                      :DEVICE_ID,
                      now()
                    );
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("PASSCODE", passcode);
            params.put("DEVICE_ID", deviceId);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static String getDeviceId(DbAccessInvoker invoker, String eventId, String passcode) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT * FROM DEVICE_PASSCODE
                    WHERE
                      EVENT_ID = :EVENT_ID
                      AND PASSCODE = :PASSCODE
                      AND (CREATED_AT + INTERVAL '1 hours') > now()
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("PASSCODE", passcode);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            if (rs.next()) {
                return rs.getString("DEVICE_ID");
            } else {
                return null;
            }
        });
    }
}
