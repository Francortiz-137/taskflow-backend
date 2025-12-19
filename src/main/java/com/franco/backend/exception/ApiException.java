package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String messageKey;
    private final Object[] args;

    protected ApiException(
            HttpStatus status,
            ErrorCode errorCode,
            String messageKey,
            Object... args
    ) {
        super(messageKey);
        this.status = status;
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.args = args;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
