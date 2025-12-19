package com.wex.purchasetransaction.auth.service;

import com.wex.purchasetransaction.auth.api.dto.LoginResponse;
import com.wex.purchasetransaction.auth.api.dto.RegisterResponse;
import com.wex.purchasetransaction.auth.repository.ApiTokenRepository;
import com.wex.purchasetransaction.auth.repository.UserRepository;
import com.wex.purchasetransaction.auth.repository.entity.ApiToken;
import com.wex.purchasetransaction.auth.repository.entity.User;
import com.wex.purchasetransaction.auth.repository.entity.UserRole;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ApiTokenRepository apiTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, ApiTokenRepository apiTokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.apiTokenRepository = apiTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(String username, String rawPassword) {

        User user = userRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Invalidate existing tokens
        apiTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        apiTokenRepository.save(new ApiToken(token, user));

        return new LoginResponse(token);
    }


    public RegisterResponse register(String username, String rawPassword) {
        User saved = userRepository.save(new User(username, passwordEncoder.encode(rawPassword), UserRole.USER));
        return new RegisterResponse(saved.getUsername(), saved.getCreatedAt());
    }

}
