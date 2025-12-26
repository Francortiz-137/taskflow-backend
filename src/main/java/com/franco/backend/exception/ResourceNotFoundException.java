package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String messageKey, Object... args) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, messageKey, args);
    }

    public static ResourceNotFoundException taskNotFound(Long id) {
        return new ResourceNotFoundException("task.notFound", id);
    }

    public static ResourceNotFoundException userNotFound(Long id) {
        return new ResourceNotFoundException("user.notFound", id);
    }

    public static ResourceNotFoundException userNotFound(String email) {
        return new ResourceNotFoundException("user.notFound", email);
    }
}
