package com.kmaebashi.kanjiro.service;
import java.util.Base64;
import java.util.UUID;

public class UuidUtil {
    private UuidUtil() {}

    public static String getUniqueId() {
        UUID uuid = UUID.randomUUID();
        return getUniqueIdByUuid(uuid);
    }

    static String getUniqueIdByUuid(UUID uuid) {
        String uuidStr = uuid.toString();
        long mostBits = uuid.getMostSignificantBits();
        String mostHex = Long.toHexString(mostBits);
        long leastBits = uuid.getLeastSignificantBits();
        String leastHex = Long.toHexString(leastBits);

        String base64Str = encodeBase64Url(mostBits, leastBits);

        return base64Str;
    }

    private static String encodeBase64Url(long mostBits, long leastBits) {
        byte[] bytes = new byte[16];

        for (int i = 0; i < 8; i++) {
            int shiftBits = 64 - ((i + 1) * 8);
            bytes[i] = (byte)((mostBits >>>shiftBits) &0xff);
        }
        for (int i = 0; i < 8; i++) {
            int shiftBits = 64 - ((i + 1) * 8);
            bytes[8 + i] = (byte)((leastBits >>>shiftBits) &0xff);
        }
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        return encoder.encodeToString(bytes);
    }
}
