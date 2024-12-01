package com.kmaebashi.kanjiro.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidUtilTest {

    @Test
    void getUniqueIdTest001() {
        UUID uuid = UUID.randomUUID();
        String uniqueId = UuidUtil.getUniqueIdByUuid(uuid);

        String uuidStr = uuid.toString().replace("-", "");

        byte[] bytes = Base64.getUrlDecoder().decode(uniqueId);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        String uniqueKeyStr = sb.toString();

        assertEquals(uuidStr, uniqueKeyStr);
    }
}