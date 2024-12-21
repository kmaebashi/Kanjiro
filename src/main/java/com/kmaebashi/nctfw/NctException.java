package com.kmaebashi.nctfw;

abstract public class NctException extends RuntimeException {
    private boolean isApi = false;

    public NctException(String message) {
        super(message);
    }

    public NctException(String message, boolean isApi) {
        super(message);
        this.isApi = isApi;
    }

    public NctException(String message, Exception cause) {
        super(message, cause);
    }

    public NctException(String message, Exception cause, boolean isApi) {
        super(message, cause);
        this.isApi = isApi;
    }

    public boolean isApi() {
        return this.isApi;
    }

    public void setApi(boolean isApi) {
        this.isApi = isApi;
    }
}
