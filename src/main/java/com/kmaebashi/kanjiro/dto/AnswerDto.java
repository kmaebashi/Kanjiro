package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDateTime;

public class AnswerDto {
    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("USER_NAME")
    public String userName;

    @TableColumn("MESSAGE")
    public String message;

    @TableColumn("IS_PROTECTED")
    public boolean isProtected;

    @TableColumn("UPDATED_AT")
    public LocalDateTime updatedAt;

    public AnswerDto() {}

    public AnswerDto(String userId, String userName, String message, boolean isProtected) {
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.isProtected = isProtected;
    }
}
