package com.franco.backend.service.impl;

import com.franco.backend.dto.auth.AuthResponse;
import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.User;
import com.franco.backend.exception.AuthenticationException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.security.PasswordService;
import com.franco.backend.security.jwt.JwtService;
import com.franco.backend.service.IAuthService;
import com.franco.backend.service.auth.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(AuthenticationException::new);

        if (!passwordService.matches(
                request.password(),
                user.getPasswordHash())
        ) {
            throw new AuthenticationException();
        }

        String refreshToken = refreshTokenService.issue(user);

        return new LoginResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt(),
            refreshToken
        );

    }

    @Override
    public UserResponse me(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        return userMapper.toResponse(user);
    }

    @Override
    public RefreshResponse refresh(RefreshRequest request) {

        RefreshTokenService.RotationResult result =
            refreshTokenService.rotate(request.refreshToken());

        String accessToken = jwtService.generateToken(
            result.user().getId(),
            result.user().getEmail(),
            result.user().getRole()
        );

        return new RefreshResponse(
            accessToken,
            result.refreshToken()
        );
    }

    @Override
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

}
