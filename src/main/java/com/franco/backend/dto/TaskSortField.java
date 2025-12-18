package com.franco.backend.dto;

import java.util.Arrays;

public enum TaskSortField {

    CREATED_AT("createdAt"),
    TITLE("title"),
    STATUS("status");

    private final String field;

    TaskSortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public static boolean isValid(String value) {
        return Arrays.stream(values())
                .anyMatch(v -> v.field.equals(value));
    }
}
