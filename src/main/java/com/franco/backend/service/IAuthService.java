package com.franco.backend.service;

import com.franco.backend.dto.auth.AuthResponse;
import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.user.ChangePasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UserResponse;

public interface IAuthService {
     LoginResponse login(LoginRequest request);

    RefreshResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    UserResponse me(Long userId);

    UserResponse register(CreateUserRequest request);

    void changeMyPassword(Long userId, ChangePasswordRequest request);
}
