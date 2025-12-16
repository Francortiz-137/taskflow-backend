package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getError() {
        return "BAD_REQUEST";
    }
}
