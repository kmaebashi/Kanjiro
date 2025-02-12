package com.kmaebashi.kanjiro.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomIdGenerator {
    private SecureRandom random;
    private static RandomIdGenerator randomIdGenerator;

    RandomIdGenerator() {
        this.random = new SecureRandom();
    }

    private String getId() {
        byte[] bytes = new byte[24];
        this.random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String ret = encoder.encodeToString(bytes);

        return ret;
    }

    private static char[] base32Chars = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '2', '3', '4', '5', '6', '7'
    };

    static String toBase32(byte[] bytes, int len) {
        String ret = "";
        for (int i = 0; i < len; i++) {
            // 全体(bytes.length * 8ビット)内の、今回切り取る開始/終了ビット位置
            int startBit = i * 5;
            int endBit = (i + 1) * 5 - 1;
            // 今回切り取るビットが含まれるbytesの添字
            int byteIdx1 = startBit / 8;
            int byteIdx2 = endBit / 8;
            if (byteIdx1 == byteIdx2) {
                int intVal = Byte.toUnsignedInt(bytes[byteIdx1]);
                int val = (intVal >> ((byteIdx1 + 1) * 8) % 5) & 0x1f;
                ret += base32Chars[val];
            } else {
                int intVal1 = Byte.toUnsignedInt(bytes[byteIdx1]);
                int intVal2 = Byte.toUnsignedInt(bytes[byteIdx2]);
                // 1バイト目、2バイト目に含まれるビット数
                int bits1 = ((byteIdx1 + 1) * 8) % 5;
                int bits2 = 5 - bits1;
                int val = ((intVal1 & ((1 << bits1) - 1)) << bits2)
                        | (intVal2 >> (8 - bits2));
                ret += base32Chars[val];
            }
        }
        return ret;
    }

    private String getRandomBase32Pri(int len) {
        // lenは8の倍数である前提
        byte[] bytes = new byte[len * 5 / 8];
        this.random.nextBytes(bytes);

        return toBase32(bytes, len);
    }

    public static String getRandomId() {
        if (randomIdGenerator == null) {
            randomIdGenerator = new RandomIdGenerator();
        }

        return randomIdGenerator.getId();
    }

    public static String getRandomBase32() {
        if (randomIdGenerator == null) {
            randomIdGenerator = new RandomIdGenerator();
        }

        return randomIdGenerator.getRandomBase32Pri(8);
    }
}
