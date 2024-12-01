package com.kmaebashi.kanjiro.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Sha256UtilTest {

    @Test
    void hashTest001() {
        String src = """
            Who killed Cock Robin?
            I, said the Sparrow,
            with my bow and arrow,
            I killed Cock Robin.
            """;
        String ret = Sha256Util.hash(src);

        assertEquals("b_Bfk3Dv8rqRqwdPG2Bp_EnmgULUHfNBWeZp1TYvFT0", ret);
    }

    @Test
    void hashTest002() {
        String src = """
            Who saw him die?
            I, said the Fly,
            with my little eye,
            I saw him die.
            """;
        String ret = Sha256Util.hash(src);

        assertEquals("uUyZ5UFExJasqTmcIUiDnkDnIsVR9q5Boe5uMMdxJpw", ret);
    }
}