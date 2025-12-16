package com.franco.backend.exception;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }

    public abstract int getStatus();
    public abstract String getError();
}
