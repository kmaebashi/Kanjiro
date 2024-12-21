package com.kmaebashi.nctfw;

public class ApiError {
    public int statusCode;
    public String message;

    public ApiError(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
