package com.wex.purchasetransaction.auth.service;

import com.wex.purchasetransaction.auth.api.dto.LoginResponse;
import com.wex.purchasetransaction.auth.api.dto.RegisterResponse;
import com.wex.purchasetransaction.auth.repository.ApiTokenRepository;
import com.wex.purchasetransaction.auth.repository.UserRepository;
import com.wex.purchasetransaction.auth.repository.entity.ApiToken;
import com.wex.purchasetransaction.auth.repository.entity.User;
import com.wex.purchasetransaction.auth.repository.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings
class AuthServiceTest {

    private static final Integer USER_ID = 1;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApiTokenRepository apiTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfullyAndInvalidatePreviousTokens() {
        User user = new User(
                "admin",
                "$encoded",
                UserRole.USER
        );
        user.setId(USER_ID);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "$encoded"))
                .thenReturn(true);

        LoginResponse response =
                authService.login("admin", "password123");

        assertNotNull(response.token());

        verify(apiTokenRepository).deleteByUserId(USER_ID);
        verify(apiTokenRepository).save(any(ApiToken.class));
    }

    @Test
    void shouldThrowWhenInvalidPassword() {
        User user = new User(
                "admin",
                "$encoded",
                UserRole.USER
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "$encoded"))
                .thenReturn(false);

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.login("admin", "wrong")
        );

        assertEquals("Invalid credentials", ex.getMessage());
        verify(apiTokenRepository, never()).save(any());
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(passwordEncoder.encode("password123"))
                .thenReturn("$encoded");

        User saved = new User(
                "newuser",
                "$encoded",
                UserRole.USER
        );
        saved.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class)))
                .thenReturn(saved);

        RegisterResponse response =
                authService.register("newuser", "password123");

        assertEquals("newuser", response.username());
        assertNotNull(response.createdAt());
    }
}
