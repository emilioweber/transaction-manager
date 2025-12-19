package com.wex.purchasetransaction.auth.api.dto;

import java.time.LocalDateTime;

public record RegisterResponse(String username, LocalDateTime createdAt) {}
