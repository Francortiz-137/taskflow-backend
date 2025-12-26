package com.franco.backend.service;

import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.user.UserResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);

    UserResponse me(String email);
}
