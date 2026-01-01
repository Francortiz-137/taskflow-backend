package com.franco.backend.controller;

import com.franco.backend.dto.user.ChangePasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UpdateUserRequest;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.security.auth.UserPrincipal;
import com.franco.backend.service.IUserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
    // POST /api/users
    // =========================
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponse> create(
            @RequestBody @Valid CreateUserRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
            .created(URI.create("/api/users/" + response.id()))
            .body(response);
    }


    // =========================
    // GET /api/users/{id}
    // =========================
    @GetMapping("/{id}")
    public UserResponse findById(
            @PathVariable @Positive(message = "{validation.id.positive}") Long id
    ) {
        return userService.findById(id);
    }

    // =========================
    // GET /api/users
    // =========================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    // =========================
    // PUT /api/users/{id}
    // =========================
    @PutMapping(
        value = "/{id}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserResponse update(
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
    // PUT /api/users/{id}/password
    // =========================

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @PathVariable
            @Positive(message = "{validation.id.positive}")
            Long id,

             @AuthenticationPrincipal UserPrincipal principal,

            @RequestBody
            @Valid
            ChangePasswordRequest request
    ) {
        userService.changePassword(id, request);
    }


}
