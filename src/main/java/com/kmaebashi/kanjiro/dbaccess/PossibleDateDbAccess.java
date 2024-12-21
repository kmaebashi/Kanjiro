package com.kmaebashi.kanjiro.dbaccess;


import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.util.HashMap;

public class PossibleDateDbAccess {
    private PossibleDateDbAccess() {}

    public static int insertPossibleDate(DbAccessInvoker invoker, String possibleDateId,
                                         String eventId, String name, int displayOrder) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO POSSIBLE_DATES (
                      POSSIBLE_DATE_ID,
                      EVENT_ID,
                      NAME,
                      DISPLAY_ORDER,
                      DELETED,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :POSSIBLE_DATE_ID,
                      :EVENT_ID,
                      :NAME,
                      :DISPLAY_ORDER,
                      FALSE,
                      NOW(),
                      NOW()
                    )
                """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("POSSIBLE_DATE_ID", possibleDateId);
            params.put("EVENT_ID", eventId);
            params.put("NAME", name);
            params.put("DISPLAY_ORDER", displayOrder);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }
}
