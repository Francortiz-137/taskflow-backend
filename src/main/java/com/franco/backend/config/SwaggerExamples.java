package com.franco.backend.config;

public class SwaggerExamples {

    public static final String NOT_FOUND = """
        {
          "timestamp": "2025-12-15T15:10:42.123Z",
          "status": 404,
          "error": "NOT_FOUND",
          "message": "Task with id 99 not found",
          "path": "/api/tasks/99"
        }
        """;

    public static final String VALIDATION_ERROR = """
        {
          "timestamp": "2025-12-15T15:10:42.123Z",
          "status": 400,
          "error": "VALIDATION_ERROR",
          "message": "title: must not be blank",
          "path": "/api/tasks"
        }
        """;
}
