package com.franco.backend.service.impl;

import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.User;
import com.franco.backend.exception.AuthenticationException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.security.PasswordService;
import com.franco.backend.service.IAuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserMapper userMapper;

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

        return new LoginResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }

    @Override
    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(email));

        return userMapper.toResponse(user);
    }

}
