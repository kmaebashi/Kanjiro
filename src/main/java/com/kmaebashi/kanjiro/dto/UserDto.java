package com.kmaebashi.kanjiro.dto;

import com.kmaebashi.dbutil.TableColumn;

public class UserDto {
    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("NAME")
    public String name;
}
