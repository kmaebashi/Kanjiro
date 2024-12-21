package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

public class AnswerUserDto {
    @TableColumn("EVENT_ID")
    public String eventId;

    @TableColumn("POSSIBLE_DATE_ID")
    public String possibleDateId;

    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("ANSWER")
    public int answer;

    @TableColumn("USER_NAME")
    public String userName;
}
