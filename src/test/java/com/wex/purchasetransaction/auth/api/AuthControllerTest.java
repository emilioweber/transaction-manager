package com.wex.purchasetransaction.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.purchasetransaction.auth.api.dto.LoginResponse;
import com.wex.purchasetransaction.auth.api.dto.RegisterResponse;
import com.wex.purchasetransaction.auth.service.AuthService;
import com.wex.purchasetransaction.config.web.RateLimitFilter;
import com.wex.purchasetransaction.config.web.TokenAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {RateLimitFilter.class, TokenAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        when(authService.login("admin", "password123"))
                .thenReturn(new LoginResponse("token-123"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "admin",
                          "password": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        when(authService.login("admin", "wrong"))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "admin",
                          "password": "wrong"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldRegisterUser() throws Exception {
        when(authService.register("newuser", "password123"))
                .thenReturn(new RegisterResponse("newuser", LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "newuser",
                          "password": "password123"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturnConflictWhenUserAlreadyExists() throws Exception {
        when(authService.register("existing", "password123"))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "existing",
                          "password": "password123"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }
}

