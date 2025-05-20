package com.javadream.SpringbootHumanitec.exception;

import java.time.LocalDateTime;

public class CustomApiException extends RuntimeException {
    private final int statusCode;
    private final String message;
    private final LocalDateTime timestamp;
    private final String location;

    public CustomApiException(int statusCode, String message, String location) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.location = location;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }
}