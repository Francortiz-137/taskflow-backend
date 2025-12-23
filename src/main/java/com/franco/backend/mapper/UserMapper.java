package com.franco.backend.mapper;

import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);
}
