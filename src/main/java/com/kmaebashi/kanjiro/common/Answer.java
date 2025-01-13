package com.kmaebashi.kanjiro.common;

public enum Answer {
    AVAILABLE(1),
    UNKNOWN(2),
    UNAVAILABLE(3);

    private final int value;

    Answer(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
