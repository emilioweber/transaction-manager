package com.wex.purchasetransaction.auth.repository.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_tokens")
public class ApiToken {

    @Id
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ApiToken() {
        this.createdAt = LocalDateTime.now();
    }

    public ApiToken(String token, User user) {
        this();
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


}
