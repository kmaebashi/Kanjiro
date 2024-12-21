package com.kmaebashi.nctfw;

public class InternalException extends NctException {
    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, boolean isApi) {
        super(message, isApi);
    }

    public InternalException(String message, Exception cause) {
        super(message, cause);
    }

    public InternalException(String message, Exception cause, boolean isApi) {
        super(message, cause, isApi);
    }
}
