package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
    }

    public static ResourceNotFoundException taskNotFound(Long id) {
        return new ResourceNotFoundException("Task with id " + id + " not found");
    }
}
