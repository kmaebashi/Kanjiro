package com.kmaebashi.kanjiro.service;

import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dto.UserDto;
import com.kmaebashi.nctfw.ServiceContext;

public class DbUtil {
    private DbUtil() {}

    public static String getOrCreateUser(ServiceContext context, String deviceId, String userName) {
        UserDto userDto = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(), deviceId);
        if (userDto == null) {
            String userId = UuidUtil.getUniqueId();
            AuthenticationDbAccess.insertUser(context.getDbAccessInvoker(), userId, userName);
            AuthenticationDbAccess.setUserToDevice(context.getDbAccessInvoker(), deviceId, userId);
            return userId;
        } else if (!userDto.name.equals(userName)) {
            AuthenticationDbAccess.updateUserName(context.getDbAccessInvoker(), userDto.userId, userName);
        }
        return userDto.userId;
    }
}
