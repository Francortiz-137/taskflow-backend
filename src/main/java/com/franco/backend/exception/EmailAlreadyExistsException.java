package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super(
            HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST,
            "user.email.exists",
            email
        );
    }
}
