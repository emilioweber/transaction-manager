package com.wex.purchasetransaction.auth.service;

import com.wex.purchasetransaction.config.dto.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

    public AuthenticatedUser get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) auth.getPrincipal();
    }
}

