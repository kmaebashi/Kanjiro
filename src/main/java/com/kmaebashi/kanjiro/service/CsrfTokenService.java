package com.kmaebashi.kanjiro.service;

import com.kmaebashi.kanjiro.dbaccess.CsrfTokenDbAccess;
import com.kmaebashi.kanjiro.util.RandomIdGenerator;
import com.kmaebashi.nctfw.ServiceInvoker;

public class CsrfTokenService {
    private CsrfTokenService() {}

    public static String getNextCsrfToken(ServiceInvoker invoker, String deviceId) {
        return invoker.invoke((context) -> {
            String csrfToken = RandomIdGenerator.getRandomId();
            CsrfTokenDbAccess.upsertCsrfToken(context.getDbAccessInvoker(), deviceId, csrfToken);
            return csrfToken;
        });
    }

    public static String getLastCsrfToken(ServiceInvoker invoker, String deviceId) {
        return invoker.invoke((context) -> {
            String csrfToken = CsrfTokenDbAccess.getCsrfToken(context.getDbAccessInvoker(), deviceId);
            return csrfToken;
        });
    }
}
