package com.kmaebashi.kanjiro.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Sha256Util {
    private Sha256Util() {}

    public static String hash(String src) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sha256Bytes = sha256.digest(src.getBytes());
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

            return encoder.encodeToString(sha256Bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
