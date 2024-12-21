package com.kmaebashi.nctfw;

public class BadRequestException extends NctException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, boolean isApi) {
        super(message, isApi);
    }
}
