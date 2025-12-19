package com.franco.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.franco.backend.dto.CreateUserRequest;
import com.franco.backend.dto.UserResponse;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.ErrorCode;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static com.franco.backend.testutil.ApiExceptionAssertions.assertApiException;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository repository;

    @Mock
    UserMapper mapper;

    @InjectMocks
    UserServiceImpl service;

    @Nested
    class CreateUser {

        @Test
        void shouldCreateUser() {
            CreateUserRequest request = new CreateUserRequest(
                "user@test.com",
                "password",
                "John"
            );

            User user = new User();
            user.setEmail("user@test.com");
            user.setName("John");

            User saved = new User();
            saved.setId(1L);
            saved.setEmail("user@test.com");
            saved.setName("John");
            saved.setRole(UserRole.USER);

            UserResponse response = new UserResponse(
                1L,
                "user@test.com",
                "John",
                UserRole.USER,
                OffsetDateTime.now(),
                OffsetDateTime.now()
            );

            when(repository.existsByEmail("user@test.com")).thenReturn(false);

            User mapped = new User();
            mapped.setEmail("user@test.com");
            mapped.setName("John");

            when(mapper.toEntity(request)).thenReturn(mapped);
            when(repository.save(any(User.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(response);

            UserResponse result = service.create(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(repository).existsByEmail("user@test.com");
            verify(repository).save(any(User.class));
            verify(mapper).toResponse(saved);
            verify(mapper).toEntity(request);

        }

        @Test
        void shouldFailWhenEmailAlreadyExists() {
            CreateUserRequest request =
                new CreateUserRequest("user@test.com", "password", "John");

            when(repository.existsByEmail("user@test.com")).thenReturn(true);

            assertApiException(
                () -> service.create(request),
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                "user.email.exists"
            );


            verify(repository).existsByEmail("user@test.com");
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(mapper);
        }

    }

    @Nested
    class FindById {

        @Test
        void shouldReturnUserWhenExists() {
            User user = new User();
            user.setId(1L);
            user.setName("John");
            user.setEmail("user@test.com");
            user.setRole(UserRole.USER);

            UserResponse response = new UserResponse(
                1L,
                "user@test.com",
                "John",
                UserRole.USER,
                OffsetDateTime.now(),
                OffsetDateTime.now()
            );

            when(repository.findById(1L)).thenReturn(Optional.of(user));
            when(mapper.toResponse(user)).thenReturn(response);

            UserResponse result = service.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("user@test.com");
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(repository).findById(1L);
            verify(mapper).toResponse(user);

        }

        @Test
        void shouldThrowWhenUserDoesNotExist() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertApiException(
                () -> service.findById(99L),
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                "user.notFound"
            );

            verify(repository).findById(99L);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnAllUsers() {
            User user1 = new User();
            user1.setId(1L);

            User user2 = new User();
            user2.setId(2L);

            when(repository.findAll()).thenReturn(List.of(user1, user2));
            when(mapper.toResponse(user1)).thenReturn(
                new UserResponse(1L, "a@test.com", "A", UserRole.USER, null, null)
            );
            when(mapper.toResponse(user2)).thenReturn(
                new UserResponse(2L, "b@test.com", "B", UserRole.USER, null, null)
            );

            List<UserResponse> result = service.findAll();

            assertThat(result).hasSize(2);
            verify(repository).findAll();
        }

        @Test
        void shouldReturnEmptyListWhenNoUsersExist() {
            when(repository.findAll()).thenReturn(List.of());

            List<UserResponse> result = service.findAll();

            assertThat(result).isEmpty();
            verify(repository).findAll();
            verifyNoInteractions(mapper);
        }
    }

}
