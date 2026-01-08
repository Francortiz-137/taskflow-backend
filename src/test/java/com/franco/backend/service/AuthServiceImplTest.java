package com.franco.backend.service;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.franco.backend.service.auth.RefreshTokenService;
import com.franco.backend.service.impl.AuthServiceImpl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordService passwordService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    JwtService jwtService;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    AuthServiceImpl authService;

    //----------------------------
    // LOGIN
    //----------------------------
    @Nested
    class Login {
        @Test
        void shouldLoginSuccessfully() {
            User user = new User();
            user.setId(1L);
            user.setEmail("user@test.com");
            user.setName("John");
            user.setPasswordHash("HASH");

            when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));
            when(passwordService.matches("password", "HASH"))
                .thenReturn(true);
            when(refreshTokenService.issue(any(User.class)))
                .thenReturn("refresh-token-123");


            LoginResponse result = authService.login(
                new LoginRequest("user@test.com", "password")
            );

            assertThat(result.email()).isEqualTo("user@test.com");
        }

        @Test
        void shouldFailWhenEmailDoesNotExist() {
            when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                authService.login(new LoginRequest("x@test.com", "password"))
            ).isInstanceOf(AuthenticationException.class);
        }

        @Test
        void shouldFailWhenPasswordIsInvalid() {
            User user = new User();
            user.setPasswordHash("HASH");

            when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));
            when(passwordService.matches(any(), any()))
                .thenReturn(false);

            assertThatThrownBy(() ->
                authService.login(new LoginRequest("x@test.com", "bad"))
            ).isInstanceOf(AuthenticationException.class);
        }
    }
    
    //----------------------------
    // REFRESH
    //----------------------------
    @Nested
    class Refresh{
        @Test
        void shouldRefreshAccessToken() {

            User user = new User();
            user.setId(1L);
            user.setEmail("user@test.com");
            user.setRole(UserRole.USER);

            when(refreshTokenService.rotate("refresh-token-123"))
                .thenReturn(new RefreshTokenService.RotationResult(
                    user,
                    "new-refresh-token"
                ));


            when(jwtService.generateToken(1L, "user@test.com", UserRole.USER))
                .thenReturn("new-access-token");

            RefreshResponse response = authService.refresh(
                new RefreshRequest("refresh-token-123")
            );

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");

        }
    }

    //----------------------------
    // Logout
    //----------------------------

    @Nested
    class Logout{
        @Test
        void shouldLogout() {

            authService.logout(new LogoutRequest("refresh-token-123"));

            verify(refreshTokenService)
                .revoke("refresh-token-123");
        }
    }
    
    //----------------------------
    // Register
    //----------------------------
    @Nested
    class Register{
        @Test
        void shouldRegisterUser() {
            CreateUserRequest request = new CreateUserRequest(
                "John Doe",
                "john@test.com",
                "password123"
            );

            User savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .passwordHash("hashed")
                .role(UserRole.USER)
                .build();

            when(passwordService.hash("password123")).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponse(savedUser)).thenReturn(
                new UserResponse(1L, 
                    "John Doe", 
                    "john@test.com", 
                    UserRole.USER, 
                    OffsetDateTime.now(), 
                    OffsetDateTime.now())
            );

            UserResponse response = authService.register(request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.role()).isEqualTo(UserRole.USER);

            verify(passwordService).hash("password123");
            verify(userRepository).save(any(User.class));
        }

    }

    //----------------------------
    // Me
    //----------------------------
    @Nested
    class Me{
        @Test
        void shouldReturnCurrentUser() {
            User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .role(UserRole.USER)
                .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(
                new UserResponse(1L, 
                    "John", 
                    "john@test.com", 
                    UserRole.USER, 
                    OffsetDateTime.now(), 
                    OffsetDateTime.now())
            );

            UserResponse response = authService.me(1L);

            assertThat(response.email()).isEqualTo("john@test.com");
        }

        @Test
        void shouldThrowIfUserNotFoundOnMe() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.me(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    //----------------------------
    // Change My Password
    //----------------------------
    @Nested
    class changeMyPassword{
        @Test
        void shouldChangePasswordWhenCurrentPasswordMatches() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPass",
                "newPass123"
            );

            User user = User.builder()
                .id(1L)
                .passwordHash("oldHash")
                .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordService.matches("oldPass", "oldHash")).thenReturn(true);
            when(passwordService.hash("newPass123")).thenReturn("newHash");

            authService.changeMyPassword(1L, request);

            assertThat(user.getPasswordHash()).isEqualTo("newHash");
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowIfCurrentPasswordIsInvalid() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongPass",
                "newPass"
            );

            User user = User.builder()
                .id(1L)
                .passwordHash("correctHash")
                .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordService.matches("wrongPass", "correctHash")).thenReturn(false);

            assertThatThrownBy(() ->
                authService.changeMyPassword(1L, request)
            ).isInstanceOf(InvalidPasswordException.class);

            verify(userRepository, never()).save(any());
        }


    }
}
