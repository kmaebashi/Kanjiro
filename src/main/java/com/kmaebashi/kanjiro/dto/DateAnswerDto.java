package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

public class DateAnswerDto {
    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("POSSIBLE_DATE_ID")
    public String possibleDateId;

    @TableColumn("ANSWER")
    public int answer;

    public DateAnswerDto() {}

    public DateAnswerDto(String userId, String possibleDateId, int answer) {
        this.userId = userId;
        this.possibleDateId = possibleDateId;
        this.answer = answer;
    }
}
