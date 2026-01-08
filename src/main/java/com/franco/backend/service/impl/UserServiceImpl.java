package com.franco.backend.service.impl;

import com.franco.backend.dto.user.ChangePasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UpdateUserRequest;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.exception.EmailAlreadyExistsException;
import com.franco.backend.exception.InvalidPasswordException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.security.PasswordService;
import com.franco.backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordService passwordService;

    @Override
    public UserResponse create(CreateUserRequest request) {
        log.info("Admin creating user email={}", request.email());

        if (repository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = mapper.toEntity(request);

        user.setPasswordHash(passwordService.hash(request.password()));
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
        
        // Fetch user
        User user = repository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));

        // Idempotent update validation - if the new password is the same as the current one, we consider it a no-op
        if (user.getPasswordHash() == null) {
            throw new IllegalStateException("User without password hash");
        }

        // Validate current password
        if (!passwordService.matches(
                request.currentPassword(),
                user.getPasswordHash())
        ) {
            throw new InvalidPasswordException();
        }

        // Hash new password
        String newHash = passwordService.hash(request.newPassword());

        // Idempotent update - check if the new password hash is the same as the current one
        if (passwordService.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("user.password.invalid");
        }

        // Update password hash
        user.setPasswordHash(newHash);
        repository.save(user);
    }


    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {

        User user = repository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));

        // Idempotent update
        if (request.name().equals(user.getName())) {
            return mapper.toResponse(user); // no save 
        }

        user.setName(request.name());
        return mapper.toResponse(repository.save(user));
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        log.info("Changing password for userId={}", id);
        User user = repository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));

        String hash = passwordService.hash(newPassword);
        user.setPasswordHash(hash);

        repository.save(user);
    }

}
