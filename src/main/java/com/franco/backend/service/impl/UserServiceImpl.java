package com.franco.backend.service.impl;

import com.franco.backend.dto.ChangePasswordRequest;
import com.franco.backend.dto.CreateUserRequest;
import com.franco.backend.dto.UserResponse;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.EmailAlreadyExistsException;
import com.franco.backend.exception.InvalidPasswordException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(CreateUserRequest request) {

        if (repository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = mapper.toEntity(request);

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        return mapper.toResponse(repository.save(user));
    }

    @Override
    public UserResponse findById(Long id) {
        User user = repository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));

        return mapper.toResponse(user);
    }

    @Override
    public List<UserResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {

        User user = repository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash())
        ) {
            throw new InvalidPasswordException();
        }

        String newHash = passwordEncoder.encode(request.newPassword());
        user.setPasswordHash(newHash);

        repository.save(user);
    }
}
