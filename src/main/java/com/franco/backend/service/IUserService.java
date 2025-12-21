package com.franco.backend.service;

import com.franco.backend.dto.ChangePasswordRequest;
import com.franco.backend.dto.CreateUserRequest;
import com.franco.backend.dto.UpdateUserRequest;
import com.franco.backend.dto.UserResponse;

import java.util.List;

public interface IUserService {

    UserResponse create(CreateUserRequest request);

    UserResponse findById(Long id);

    List<UserResponse> findAll();

    void changePassword(Long id, ChangePasswordRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

}
