package com.kmaebashi.kanjiro.dbaccess;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;

public class CsrfTokenDbAccess {
    private CsrfTokenDbAccess() {}

    public static int upsertCsrfToken(DbAccessInvoker invoker,
                                      String deviceId, String csrfToken) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO CSRF_TOKENS (
                      DEVICE_ID,
                      CSRF_TOKEN,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :DEVICE_ID,
                      :CSRF_TOKEN,
                      now(),
                      now()
                    ) ON CONFLICT(DEVICE_ID)
                    DO UPDATE SET
                      DEVICE_ID = EXCLUDED.DEVICE_ID,
                      CSRF_TOKEN = EXCLUDED.CSRF_TOKEN,
                      UPDATED_AT = EXCLUDED.UPDATED_AT
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("DEVICE_ID", deviceId);
            params.put("CSRF_TOKEN", csrfToken);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static String getCsrfToken(DbAccessInvoker invoker, String deviceId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      CSRF_TOKEN
                    FROM CSRF_TOKENS
                    WHERE DEVICE_ID = :DEVICE_ID
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
            return rs.getString("CSRF_TOKEN");
        });
    }
}
