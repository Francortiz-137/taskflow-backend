package com.franco.backend.service;

import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
}
