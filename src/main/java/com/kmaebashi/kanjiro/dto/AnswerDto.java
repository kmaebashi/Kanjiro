package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

public class AnswerDto {
    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("USER_NAME")
    public String userName;

    @TableColumn("MESSAGE")
    public String message;

    @TableColumn("IS_PROTECTED")
    public boolean isProtected;
}
