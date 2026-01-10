package com.franco.backend.service.impl;

import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.user.ChangePasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.AuthenticationException;
import com.franco.backend.exception.InvalidPasswordException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.security.PasswordService;
import com.franco.backend.security.jwt.JwtService;
import com.franco.backend.service.IAuthService;
import com.franco.backend.service.auth.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Override
    public UserResponse register(CreateUserRequest request) {

        User user = User.builder()
            .name(request.name())
            .email(request.email())
            .passwordHash(passwordService.hash(request.password()))
            .role(UserRole.USER)
            .build();

        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }


    @Override
    public LoginResponse login(LoginRequest request) {

        log.debug("Authenticating email={}", request.email());
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> {
                log.warn("Login failed: user not found");
                return new AuthenticationException();
            });

        if (!passwordService.matches(
                request.password(),
                user.getPasswordHash())
        ) {
            log.warn("Login failed: invalid password for userId={}", user.getId());
            throw new AuthenticationException();
        }

        String refreshToken = refreshTokenService.issue(user);

        log.info("User {} logged in successfully", user.getId());
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
    @Transactional(readOnly = true)
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

    @Override
    public void changeMyPassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        if (!passwordService.matches(
                request.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new InvalidPasswordException();
        }

        user.setPasswordHash(
            passwordService.hash(request.newPassword())
        );

        userRepository.save(user);
    }

}
