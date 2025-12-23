package com.franco.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.franco.backend.dto.user.ChangePasswordRequest;
import com.franco.backend.dto.user.CreateUserRequest;
import com.franco.backend.dto.user.UpdateUserRequest;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.ErrorCode;
import com.franco.backend.mapper.UserMapper;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.security.PasswordService;
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

    @Mock
    PasswordService passwordService;

    @Nested
    class CreateUser {

        @Test
        void shouldCreateUser() {
            CreateUserRequest request = new CreateUserRequest(
                "John",
                "user@test.com",
                "password"
            );

            User user = new User();
            user.setEmail("user@test.com");
            user.setName("John");

            User saved = new User();
            saved.setId(1L);
            saved.setEmail(request.email());
            saved.setName(request.name());
            saved.setRole(UserRole.USER);

            UserResponse response = new UserResponse(
                1L,
                request.name(),
                request.email(),
                UserRole.USER,
                OffsetDateTime.now(),
                OffsetDateTime.now()
            );

            when(mapper.toEntity(request)).thenReturn(user);
            when(repository.save(user)).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(response);
            when(passwordService.hash("password")).thenReturn("NEW_HASH");

            UserResponse result = service.create(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("John");
            assertThat(result.email()).isEqualTo("user@test.com");
            assertThat(result.role()).isEqualTo(UserRole.USER);
            assertThat(user.getPasswordHash()).isEqualTo("NEW_HASH");

            verify(mapper).toEntity(request);
            verify(repository).save(user);
            verify(mapper).toResponse(saved);
            verify(passwordService).hash("password");
        }

        @Test
        void shouldFailWhenEmailAlreadyExists() {
            CreateUserRequest request =
                new CreateUserRequest("John", "user@test.com", "password");

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
                "John",
                "user@test.com",
                UserRole.USER,
                OffsetDateTime.now(),
                OffsetDateTime.now()
            );

            when(repository.findById(1L)).thenReturn(Optional.of(user));
            when(mapper.toResponse(user)).thenReturn(response);

            UserResponse result = service.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("user@test.com");
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
            verify(mapper, times(2)).toResponse(any());
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

    @Nested
    class ChangePassword {

        @Test
        void shouldChangePasswordSuccessfully() {
            ChangePasswordRequest request =
                new ChangePasswordRequest("oldPass", "newSecurePass");

            User user = new User();
            user.setId(1L);
            user.setPasswordHash("$2a$10$OLD_HASH");

            when(repository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordService.matches("oldPass", "$2a$10$OLD_HASH"))
                    .thenReturn(true);

            when(passwordService.matches("newSecurePass", "$2a$10$OLD_HASH"))
                    .thenReturn(false);

            when(passwordService.hash("newSecurePass"))
                    .thenReturn("NEW_HASH");

            service.changePassword(1L, request);

            verify(passwordService).matches("oldPass", "$2a$10$OLD_HASH");
            verify(passwordService).hash("newSecurePass");
            verify(repository).save(user);

            assertThat(user.getPasswordHash()).isEqualTo("NEW_HASH");
            assertThat(user.getPasswordHash()).isNotEqualTo("newSecurePass");
        }

        @Test
        void shouldFailWhenUserDoesNotExist() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertApiException(
                () -> service.changePassword(
                    99L, new ChangePasswordRequest("old", "new")
                ),
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                "user.notFound"
            );
        }

        @Test
        void shouldFailWhenCurrentPasswordIsIncorrect() {
            User user = new User();
            user.setId(1L);
            user.setPasswordHash("$2a$10$HASH");

            when(repository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordService.matches(any(), any())).thenReturn(false);

            assertApiException(
                () -> service.changePassword(
                    1L, new ChangePasswordRequest("wrong", "new")
                ),
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                "user.password.invalid"
            );

            verify(repository, never()).save(any());
            verify(passwordService, never()).hash(any());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateUserName() {
            UpdateUserRequest request = new UpdateUserRequest("New Name");

            User existing = new User();
            existing.setId(1L);
            existing.setEmail("user@test.com");
            existing.setName("Old Name");
            existing.setRole(UserRole.USER);

            User saved = new User();
            saved.setId(1L);
            saved.setEmail("user@test.com");
            saved.setName("New Name");
            saved.setRole(UserRole.USER);

            UserResponse response = new UserResponse(
                1L,
                "New Name",
                "user@test.com",
                UserRole.USER,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now()
            );

            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(response);

            UserResponse result = service.update(1L, request);

            assertThat(result.name()).isEqualTo("New Name");
            assertThat(result.email()).isEqualTo("user@test.com");
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(repository).findById(1L);
            verify(repository).save(existing);
            verify(mapper).toResponse(saved);
        }

        @Test
        void shouldThrowWhenUserDoesNotExist() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertApiException(
                () -> service.update(99L, new UpdateUserRequest("Name")),
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                "user.notFound"
            );

            verify(repository).findById(99L);
            verifyNoInteractions(mapper);
        }

    }

    @Nested
    class UpdatePassword {

        @Test
        void shouldHashPasswordWhenCreatingUser() {
            CreateUserRequest request = new CreateUserRequest(
                "John",
                "user@test.com",
                "password123"
            );

            User user = new User();
            user.setEmail("user@test.com");
            user.setName("John");

            User saved = new User();
            saved.setId(1L);
            saved.setEmail("user@test.com");
            saved.setName("John");
            saved.setRole(UserRole.USER);

            when(repository.existsByEmail("user@test.com")).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(user);
            when(repository.save(any(User.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(
                new UserResponse(1L, "John", "user@test.com", UserRole.USER, null, null)
            );

            when(passwordService.hash("password123")).thenReturn("HASHED");

            service.create(request);

            verify(passwordService).hash("password123");
            verify(repository).save(argThat(u ->
                "HASHED".equals(u.getPasswordHash())
            ));
        }
    }
}
