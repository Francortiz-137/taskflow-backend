package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ApiException {

    public InvalidPasswordException() {
        super(
            HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST,
            "user.password.invalid"
        );
    }
}
