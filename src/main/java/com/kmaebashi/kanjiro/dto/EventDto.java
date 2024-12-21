package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDateTime;

public class EventDto {
    @TableColumn("EVENT_ID")
    public String eventId;

    @TableColumn("ORGANIZER_ID")
    public String organizierId;

    @TableColumn("EVENT_NAME")
    public String eventName;

    @TableColumn("DESCRIPTION")
    public String description;

    @TableColumn("FIXED_DATE_ID")
    public String fixedDateId;

    @TableColumn("IS_SECRET_MODE")
    public boolean isSecretMode;

    @TableColumn("IS_AUTO_SCHEDULE")
    public boolean isAutoSchedule;

    @TableColumn("UPDATED_AT")
    public LocalDateTime updatedAt;
}
