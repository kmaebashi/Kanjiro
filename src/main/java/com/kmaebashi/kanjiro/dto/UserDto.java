package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

public class UserDto {
    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("NAME")
    public String name;

    @TableColumn("LOGIN_ID")
    public String loginId;

    @TableColumn("PASSWORD")
    public String password;
}
