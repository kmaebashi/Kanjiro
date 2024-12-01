package com.kmaebashi.kanjiro.dto;
import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDateTime;

public class DeviceDto {
    @TableColumn("DEVICE_ID")
    public String deviceId;

    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("LAST_LOGIN")
    public LocalDateTime lastLogin;

    @TableColumn("SECRET_KEY")
    public String secretKey;
}
