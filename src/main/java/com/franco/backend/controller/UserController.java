package com.franco.backend.controller;

import com.franco.backend.config.SwaggerExamples;
import com.franco.backend.dto.common.ApiErrorResponse;
import com.franco.backend.dto.user.AdminResetPasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UpdateUserRequest;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Users (Admin)", description = "Admin-only user management endpoints")
@RestController
@RequestMapping(
    value = "/api/users",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
@Validated
public class UserController {

    private final IUserService userService;

    // =========================
    // POST /api/users (ADMIN)
    // =========================

    @Operation(summary = "Create a user (admin only)")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.VALIDATION_ERROR)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (missing/invalid token)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (admin role required)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> create(
            @RequestBody @Valid CreateUserRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
            .created(URI.create("/api/users/" + response.id()))
            .body(response);
    }

    // =========================
    // GET /api/users/{id} (ADMIN)
    // =========================

    @Operation(summary = "Get a user by ID (admin only)")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.VALIDATION_ERROR)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (missing/invalid token)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (admin role required)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserResponse findById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable
            @Positive(message = "{validation.id.positive}")
            Long id
    ) {
        return userService.findById(id);
    }

    // =========================
    // GET /api/users (ADMIN)
    // =========================

    @Operation(summary = "List all users (admin only)")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "List of users",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (missing/invalid token)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (admin role required)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    // =========================
    // PUT /api/users/{id} (ADMIN)
    // =========================

    @Operation(summary = "Update a user (admin only)")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.VALIDATION_ERROR)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (missing/invalid token)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (admin role required)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
        value = "/{id}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse update(
            @Parameter(description = "User ID", example = "1")
            @PathVariable
            @Positive(message = "{validation.id.positive}")
            Long id,

            @RequestBody
            @Valid
            UpdateUserRequest request
    ) {
        return userService.update(id, request);
    }

    // =========================
    // PUT /api/users/{id}/password (ADMIN)
    // =========================

    @Operation(
        summary = "Reset a user's password (admin only)",
        description = "Allows an admin to reset a user's password without knowing the current one"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Password changed successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.VALIDATION_ERROR)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (missing/invalid token)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (admin role required)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
        value = "/{id}/password",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(
            @PathVariable
            @Positive(message = "{validation.id.positive}")
            Long id,

            @RequestBody
            @Valid
            AdminResetPasswordRequest request
    ) {
        userService.resetPassword(id, request.newPassword());
    }
}
