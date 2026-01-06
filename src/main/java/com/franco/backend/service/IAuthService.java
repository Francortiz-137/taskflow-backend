package com.franco.backend.service;

import com.franco.backend.dto.auth.AuthResponse;
import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.user.UserResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);

    UserResponse me(Long userId);
    RefreshResponse refresh(RefreshRequest request);
    void logout(LogoutRequest request);

}
