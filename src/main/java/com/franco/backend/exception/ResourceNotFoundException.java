package com.franco.backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.NOT_FOUND.value();
    }

    @Override
    public String getError() {
        return "NOT_FOUND";
    }

    public static ResourceNotFoundException taskNotFound(Long id) {
        return new ResourceNotFoundException("Task with id " + id + " not found");
    }

}
