package com.wex.purchasetransaction.config.web;

import com.wex.purchasetransaction.auth.repository.entity.ApiToken;
import com.wex.purchasetransaction.auth.repository.ApiTokenRepository;
import com.wex.purchasetransaction.auth.repository.entity.User;
import com.wex.purchasetransaction.config.dto.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final ApiTokenRepository apiTokenRepository;
    private final long tokenTtlMinutes;

    public TokenAuthenticationFilter(
            ApiTokenRepository apiTokenRepository,
            @Value("${security.auth.token-ttl-minutes:60}") long tokenTtlMinutes) {
        this.apiTokenRepository = apiTokenRepository;
        this.tokenTtlMinutes = tokenTtlMinutes;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String tokenValue = authHeader.substring(7);

            apiTokenRepository.findById(tokenValue).ifPresent(apiToken -> {

                if (isExpired(apiToken)) {
                    apiTokenRepository.delete(apiToken);
                    SecurityContextHolder.clearContext();
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = getAuthentication(apiToken);

                if (authentication != null) {
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private static UsernamePasswordAuthenticationToken getAuthentication(ApiToken apiToken) {
        User user = apiToken.getUser();

        if (user == null || user.getRole() == null) {
            return null;
        }

        AuthenticatedUser principal = new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getRole().getRoleName()
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName())
                )
        );
    }

    private boolean isExpired(ApiToken token) {
        return token.getCreatedAt()
                .plusMinutes(tokenTtlMinutes)
                .isBefore(LocalDateTime.now());
    }
}

