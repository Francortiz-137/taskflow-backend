package com.franco.backend.service.auth;

import com.franco.backend.entity.User;

public interface RefreshTokenService {

    record RotationResult(
        User user,
        String refreshToken
    ) {}

    String issue(User user);
    RotationResult rotate(String refreshToken);
    void revoke(String refreshToken);
}
