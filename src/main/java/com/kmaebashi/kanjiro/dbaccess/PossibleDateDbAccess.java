package com.kmaebashi.kanjiro.dbaccess;


import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.kanjiro.dto.PossibleDateDto;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

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

    public static List<PossibleDateDto> getPossbleDates(DbAccessInvoker invoker, String eventId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      POSSIBLE_DATE_ID,
                      EVENT_ID,
                      NAME,
                      DISPLAY_ORDER
                    FROM POSSIBLE_DATES
                    WHERE
                      EVENT_ID = :EVENT_ID
                      AND DELETED <> TRUE
                    ORDER BY DISPLAY_ORDER
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<PossibleDateDto> dtoList = ResultSetMapper.toDtoList(rs, PossibleDateDto.class);

            return dtoList;
        });
    }
}
