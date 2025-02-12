package com.kmaebashi.kanjiro.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomIdGeneratorTest {
    @Test
    void toBase32Test001() {
        byte[] bytes = new byte[5];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)0xcd;
        }
        String ret = RandomIdGenerator.toBase32(bytes, 8);
        assertEquals("ZXG43TON", ret);
    }

    @Test
    void getRandomBase32Test001() {
        String ret = RandomIdGenerator.getRandomBase32();
        assertEquals(8, ret.length());
    }
}