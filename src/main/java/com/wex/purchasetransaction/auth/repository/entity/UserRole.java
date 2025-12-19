package com.wex.purchasetransaction.auth.repository.entity;

public enum UserRole {
    USER("USER");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
