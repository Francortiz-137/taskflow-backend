package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String messageKey, Object... args) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, messageKey, args);
    }
}
