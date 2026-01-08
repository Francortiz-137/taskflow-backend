package com.franco.backend.controller;

import com.franco.backend.config.SwaggerExamples;
import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.common.ApiErrorResponse;
import com.franco.backend.dto.user.ChangePasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.security.auth.UserPrincipal;
import com.franco.backend.service.IAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Authentication", description = "Authentication and self-service endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    // =========================
    // POST /api/auth/register
    // =========================
    @Operation(summary = "Register a new user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = SwaggerExamples.VALIDATION_ERROR)
            )
        )
    })
    @PostMapping(
        value = "/register",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
        @RequestBody @Valid CreateUserRequest request
    ) {
        return authService.register(request);
    }

    // =========================
    // POST /api/auth/login
    // =========================
    @Operation(summary = "Login user")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login successful"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error"
        )
    })
    @PostMapping(
        value = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LoginResponse login(
        @RequestBody @Valid LoginRequest request
    ) {
        log.info("Login attempt for email={}", request.email());
        return authService.login(request);
    }

    // =========================
    // POST /api/auth/refresh
    // =========================

    @Operation(
        summary = "Refresh access token",
        description = "Generates a new access token using a valid refresh token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token"
        )
    })
    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @RequestBody @Valid RefreshRequest request
    ) {
        log.info("Refreshing token");
        return authService.refresh(request);
    }

    // =========================
    // POST /api/auth/logout
    // =========================

    @Operation(
        summary = "Logout user",
        description = "Invalidates the refresh token and logs out the user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Logout successful"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        )
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody @Valid LogoutRequest request) {
        log.info("Logout requested");
        authService.logout(request);
    }

    // =========================
    // GET /api/auth/me
    // =========================
    @Operation(
        summary = "Get current authenticated user",
        description = "Returns information about the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Authenticated user info"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        )
    })
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        log.debug("Fetching profile for userId={}", principal.id());
        return authService.me(principal.id());
    }

    // =========================
    // PUT /api/auth/me/password
    // =========================
    @Operation(summary = "Change own password")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password updated"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @PutMapping(
        value = "/me/password",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyPassword(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody @Valid ChangePasswordRequest request
    ) {
        authService.changeMyPassword(principal.id(), request);
    }
}
