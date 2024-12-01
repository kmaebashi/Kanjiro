package com.kmaebashi.kanjiro.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomIdGeneratorTest {

    @Test
    void getRandomIdTest001() {
        String randomId1 = RandomIdGenerator.getRandomId();
        String randomId2 = RandomIdGenerator.getRandomId();
        assertNotEquals(randomId1, randomId2);
        assertEquals(32, randomId1.length());
        assertEquals(32, randomId2.length());
    }
}