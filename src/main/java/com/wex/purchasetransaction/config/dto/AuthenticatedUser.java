package com.wex.purchasetransaction.config.dto;

public record AuthenticatedUser(
    Integer id,
    String username,
    String role
) {}
