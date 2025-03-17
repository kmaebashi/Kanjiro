package com.kmaebashi.kanjiro.dbaccess;

import java.time.LocalDateTime;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.kanjiro.dto.DeviceDto;
import com.kmaebashi.kanjiro.dto.EventDto;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;

public class EventDbAccess {
    public static int insertEvent(DbAccessInvoker invoker,
                                  String eventId, String organizerName, String organizerId, String eventName,
                                  String description, LocalDateTime deadline, String appendTime,
                                  boolean isSecretMode, boolean isAutoSchedule) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO EVENTS (
                      EVENT_ID,
                      ORGANIZER_NAME,
                      ORGANIZER_ID,
                      EVENT_NAME,
                      DESCRIPTION,
                      DEADLINE,
                      SCHEDULE_APPEND_TIME,
                      IS_SECRET_MODE,
                      IS_AUTO_SCHEDULE,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :EVENT_ID,
                      :ORGANIZER_NAME,
                      :ORGANIZER_ID,
                      :EVENT_NAME,
                      :DESCRIPTION,
                      :DEADLINE,
                      :APPEND_TIME,
                      :IS_SECRET_MODE,
                      :IS_AUTO_SCHEDULE,
                      NOW(),
                      NOW()
                    )
                """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("ORGANIZER_NAME", organizerName);
            params.put("ORGANIZER_ID", organizerId);
            params.put("EVENT_NAME", eventName);
            params.put("DESCRIPTION", description);
            params.put("DEADLINE", deadline);
            params.put("APPEND_TIME", appendTime);
            params.put("IS_SECRET_MODE", isSecretMode);
            params.put("IS_AUTO_SCHEDULE", isAutoSchedule);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static EventDto getEvent(DbAccessInvoker invoker, String eventId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM EVENTS
                    WHERE EVENT_ID = :EVENT_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            EventDto dto = ResultSetMapper.toDto(rs, EventDto.class);

            return dto;
        });
    }

    public static int updateEvent(DbAccessInvoker invoker,
                                  String eventId, String organizerName, String eventName, String description,
                                  LocalDateTime deadline, String appendTime, String fixedDate,
                                  boolean isSecretMode, boolean isAutoSchedule) {
        return invoker.invoke((context) -> {
            String sql = """
                    UPDATE EVENTS SET
                      ORGANIZER_NAME = :ORGANIZER_NAME,
                      EVENT_NAME = :EVENT_NAME,
                      DESCRIPTION = :DESCRIPTION,
                      DEADLINE = :DEADLINE,
                      FIXED_DATE_ID = :FIXED_DATE_ID,
                      SCHEDULE_APPEND_TIME = :SCHEDULE_APPEND_TIME,
                      IS_SECRET_MODE = :IS_SECRET_MODE,
                      IS_AUTO_SCHEDULE = :IS_AUTO_SCHEDULE,
                      UPDATED_AT = now()
                    WHERE
                      EVENT_ID = :EVENT_ID
                """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("EVENT_ID", eventId);
            params.put("ORGANIZER_NAME", organizerName);
            params.put("EVENT_NAME", eventName);
            params.put("DESCRIPTION", description);
            params.put("DEADLINE", deadline);
            params.put("FIXED_DATE_ID", fixedDate);
            params.put("SCHEDULE_APPEND_TIME", appendTime);
            params.put("IS_SECRET_MODE", isSecretMode);
            params.put("IS_AUTO_SCHEDULE", isAutoSchedule);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

}
