package com.franco.backend.controller;

import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.security.auth.UserPrincipal;
import com.franco.backend.service.IAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Authentication and session management")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

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
        return authService.login(request);
    }

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
        return authService.me(principal.id());
    }

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
        return authService.refresh(request);
    }

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
        authService.logout(request);
    }

    
}
