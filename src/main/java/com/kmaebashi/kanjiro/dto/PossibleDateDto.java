package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

public class PossibleDateDto {
    @TableColumn("POSSIBLE_DATE_ID")
    public String possibleDateId;

    @TableColumn("EVENT_ID")
    public String eventId;

    @TableColumn("NAME")
    public String name;

    @TableColumn("DISPLAY_ORDER")
    public int displayOrder;

    public PossibleDateDto() {}

    public PossibleDateDto(String possibleDateId, String eventId, String name, int displayOrder) {
        this.possibleDateId = possibleDateId;
        this.eventId = eventId;
        this.name = name;
        this.displayOrder = displayOrder;
    }
}
