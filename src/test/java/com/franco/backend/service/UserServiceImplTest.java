package com.franco.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.franco.backend.api.GlobalExceptionHandler;
import com.franco.backend.config.SecurityConfig;
import com.franco.backend.dto.ChangePasswordRequest;
import com.franco.backend.dto.CreateUserRequest;
import com.franco.backend.dto.TaskResponse;
import com.franco.backend.dto.UpdateUserRequest;
import com.franco.backend.dto.UserResponse;
import com.franco.backend.entity.TaskStatus;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.franco.backend.testutil.ApiExceptionAssertions.assertApiException;

@ExtendWith(MockitoExtension.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UserServiceImplTest {

    @Mock
    UserRepository repository;

    @Mock
    UserMapper mapper;

    @InjectMocks
    UserServiceImpl service;

    @Mock
    PasswordEncoder passwordEncoder;


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
            when(passwordEncoder.encode("password")).thenReturn("NEW_HASH");


            UserResponse result = service.create(request);  
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("John");
            assertThat(result.email()).isEqualTo("user@test.com");
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(mapper).toEntity(request);
            verify(repository).save(user);
            verify(mapper).toResponse(saved);

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
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("NEW_HASH");


        service.changePassword(1L, request);

        verify(repository).save(user);
        assertThat(user.getPasswordHash())
            .isNotEqualTo("newSecurePass");
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertApiException(
            () -> service.changePassword(99L,
                new ChangePasswordRequest("old", "new")),
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
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertApiException(
            () -> service.changePassword(1L,
                new ChangePasswordRequest("wrong", "new")),
            HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST,
            "user.password.invalid"
        );
    }
}

    @Nested
class UpdateUser {

    @Test
    void shouldUpdateUserName() {
        // given
        UpdateUserRequest request = new UpdateUserRequest("New Name", null);

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

        // when
        UserResponse result = service.update(1L, request);

        // then
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
            () -> service.update(99L, new UpdateUserRequest("Name",null)),
            HttpStatus.NOT_FOUND,
            ErrorCode.NOT_FOUND,
            "user.notFound"
        );

        verify(repository).findById(99L);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldThrowWhenRequestIsEmpty() {
        User user = new User();
        user.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        assertApiException(
            () -> service.update(1L, new UpdateUserRequest(null, null)),
            HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST,
            "validation.emptyUpdate"
        );

        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldNotAllowEmailUpdate() {
        UpdateUserRequest request = new UpdateUserRequest(
            "New Name",
            "new@email.com" // intento malicioso
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("Old Name");

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        assertApiException(
            () -> service.update(1L, request),
            HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST,
            "user.email.updateNotAllowed"
        );

        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }
}

}
