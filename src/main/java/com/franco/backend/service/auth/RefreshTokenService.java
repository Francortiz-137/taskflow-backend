package com.franco.backend.service.auth;

import com.franco.backend.entity.User;

public interface RefreshTokenService {
    String issue(User user);
    User validateAndGetUser(String refreshToken);
    void revoke(String refreshToken);
}
