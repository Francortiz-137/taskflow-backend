package com.franco.backend.controller;

import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.service.IAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

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

    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        return authService.me(principal.getName());
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @RequestBody @Valid RefreshRequest request
    ) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody @Valid LogoutRequest request) {
        authService.logout(request);
    }

    
}
