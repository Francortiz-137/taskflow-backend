package com.franco.backend.service;

import com.franco.backend.dto.user.ChangePasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UpdateUserRequest;
import com.franco.backend.dto.user.UserResponse;

import java.util.List;

public interface IUserService {

    UserResponse create(CreateUserRequest request);

    UserResponse findById(Long id);

    List<UserResponse> findAll();

    void changePassword(Long id, ChangePasswordRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    void resetPassword(Long id, String newPassword);

}
