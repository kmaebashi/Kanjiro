package com.kmaebashi.kanjiro.dbaccess;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.kanjiro.dto.AnswerDto;
import com.kmaebashi.kanjiro.dto.DateAnswerDto;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class AnswerDbAccess {
    private AnswerDbAccess() {}

    public static List<AnswerDto> getAnswers(DbAccessInvoker invoker, String eventId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      USER_ID,
                      USER_NAME,
                      MESSAGE,
                      IS_PROTECTED,
                      UPDATED_AT
                    FROM ANSWERS
                    WHERE
                      EVENT_ID = :EVENT_ID
                    ORDER BY CREATED_AT
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<AnswerDto> dtoList = ResultSetMapper.toDtoList(rs, AnswerDto.class);

            return dtoList;
        });
    }

    public static AnswerDto getAnswer(DbAccessInvoker invoker, String eventId, String userId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      USER_ID,
                      USER_NAME,
                      MESSAGE,
                      IS_PROTECTED,
                      UPDATED_AT
                    FROM ANSWERS
                    WHERE
                      EVENT_ID = :EVENT_ID
                      AND USER_ID = :USER_ID
                    ORDER BY CREATED_AT
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("USER_ID", userId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            AnswerDto dto = ResultSetMapper.toDto(rs, AnswerDto.class);

            return dto;
        });
    }
    public static List<DateAnswerDto> getDateAnswers(DbAccessInvoker invoker, String eventId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      USER_ID,
                      POSSIBLE_DATE_ID,
                      ANSWER
                    FROM DATE_ANSWERS
                    WHERE
                      EVENT_ID = :EVENT_ID
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<DateAnswerDto> dtoList = ResultSetMapper.toDtoList(rs, DateAnswerDto.class);

            return dtoList;
        });
    }

    public static int insertAnswer(DbAccessInvoker invoker, String eventId, String userId, String userName,
                                   String message, boolean isProtected) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO ANSWERS (
                      EVENT_ID,
                      USER_ID,
                      USER_NAME,
                      MESSAGE,
                      IS_PROTECTED,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :EVENT_ID,
                      :USER_ID,
                      :USER_NAME,
                      :MESSAGE,
                      :IS_PROTECTED,
                      NOW(),
                      NOW()
                    )
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("USER_ID", userId);
            params.put("USER_NAME", userName);
            params.put("MESSAGE", message);
            params.put("IS_PROTECTED", isProtected);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int upsertAnswer(DbAccessInvoker invoker, String eventId, String userId, String userName,
                                   String message, boolean isProtected) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO ANSWERS (
                      EVENT_ID,
                      USER_ID,
                      USER_NAME,
                      MESSAGE,
                      IS_PROTECTED,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :EVENT_ID,
                      :USER_ID,
                      :USER_NAME,
                      :MESSAGE,
                      :IS_PROTECTED,
                      now(),
                      now()
                    ) ON CONFLICT (EVENT_ID, USER_ID)
                    DO UPDATE SET
                      USER_NAME = EXCLUDED.USER_NAME,
                      MESSAGE = EXCLUDED.MESSAGE,
                      IS_PROTECTED = EXCLUDED.IS_PROTECTED,
                      UPDATED_AT = EXCLUDED.UPDATED_AT
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("USER_ID", userId);
            params.put("USER_NAME", userName);
            params.put("MESSAGE", message);
            params.put("IS_PROTECTED", isProtected);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int insertDateAnswer(DbAccessInvoker invoker, String eventId, String userId,
                                       String possibleDateId, int answer) {
        return invoker.invoke((context) -> {
            String sql = """
                INSERT INTO DATE_ANSWERS (
                  EVENT_ID,
                  USER_ID,
                  POSSIBLE_DATE_ID,
                  ANSWER
                ) VALUES (
                  :EVENT_ID,
                  :USER_ID,
                  :POSSIBLE_DATE_ID,
                  :ANSWER
                )
                """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("USER_ID", userId);
            params.put("POSSIBLE_DATE_ID", possibleDateId);
            params.put("ANSWER", answer);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int deleteAnswer(DbAccessInvoker invoker, String eventId, String userId) {
        return invoker.invoke((context) -> {
            String sql = """
                    DELETE FROM ANSWERS
                    WHERE
                      EVENT_ID = :EVENT_ID
                      AND USER_ID = :USER_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("USER_ID", userId);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int deleteDateAnswer(DbAccessInvoker invoker, String eventId, String userId) {
        return invoker.invoke((context) -> {
            String sql = """
                    DELETE FROM DATE_ANSWERS
                    WHERE
                      EVENT_ID = :EVENT_ID
                      AND USER_ID = :USER_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("USER_ID", userId);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }
}
