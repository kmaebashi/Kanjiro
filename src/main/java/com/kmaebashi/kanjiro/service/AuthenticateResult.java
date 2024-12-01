package com.kmaebashi.kanjiro.service;

import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.nctfw.RedirectResult;

public class AuthenticateResult {
    public String deviceId;
    public String authCookie;

    public AuthenticateResult(String deviceId, String authCookie) {
        this.deviceId = deviceId;
        this.authCookie = authCookie;
    }
}
