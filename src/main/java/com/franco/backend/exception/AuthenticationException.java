package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {

    public AuthenticationException() {
        super(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.UNAUTHORIZED,
            "auth.invalid.credentials"
        );
    }
}
