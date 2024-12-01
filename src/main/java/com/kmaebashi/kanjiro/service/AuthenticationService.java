package com.kmaebashi.kanjiro.service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.kmaebashi.kanjiro.common.SessionKey;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dto.DeviceDto;
import com.kmaebashi.kanjiro.util.Log;
import com.kmaebashi.nctfw.ServiceContext;
import jakarta.servlet.http.HttpSession;

import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.ServiceInvoker;

public class AuthenticationService {
    private AuthenticationService() {}

    public static AuthenticateResult authenticateDevice(ServiceInvoker invoker, String authCookie) {
        return invoker.invoke((context) -> {
            String newDeviceId;
            String[] deviceIdOut = new String[1];
            if (checkAuthCookie(context, authCookie, deviceIdOut)) {
                newDeviceId = deviceIdOut[0];
            } else {
                newDeviceId = UuidUtil.getUniqueId();
            }
            String newAuthCookie = generateAuthCookie(context, newDeviceId);
            AuthenticateResult ret = new AuthenticateResult(newDeviceId, newAuthCookie);

            return ret;
        });
    }

    private static DateTimeFormatter lastLoginFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static boolean checkAuthCookie(ServiceContext context, String authCookie, String[] deviceIdOut) {
        if (authCookie == null) {
            return false;
        }
        String[] cookieFields = authCookie.split(":");
        if (cookieFields.length != 3) {
            return false;
        }
        String deviceId = cookieFields[0];
        String lastLoginStr = cookieFields[1];
        String signature = cookieFields[2];
        DeviceDto deviceDto = AuthenticationDbAccess.getDevice(context.getDbAccessInvoker(), deviceId);
        if (deviceDto == null) {
            Log.warn("Cookieで送信されたdeviceIdがDBに存在しません(" + deviceId + ")。");
            return false;
        }
        String dbLastLogin = lastLoginFormatter.format(deviceDto.lastLogin);
        if (!lastLoginStr.equals(dbLastLogin)) {
            return false;
        }
        String expected = Sha256Util.hash(deviceId + ":" + dbLastLogin + ":" + deviceDto.secretKey);

        deviceIdOut[0] = deviceId;
        return signature.equals(expected);
    }

    private static String generateAuthCookie(ServiceContext context, String deviceId) {
        LocalDateTime lastLogin = LocalDateTime.now();
        String lastLoginStr = lastLoginFormatter.format(lastLogin);
        String secretKey = RandomIdGenerator.getRandomId();
        String signature = Sha256Util.hash(deviceId + ":" + lastLoginStr + ":" + secretKey);
        String cookieStr = deviceId + ":" + lastLoginStr + ":" + signature;

        AuthenticationDbAccess.upsertDevice(context.getDbAccessInvoker(),
                                            deviceId, lastLogin, secretKey);

        return cookieStr;
    }
}
