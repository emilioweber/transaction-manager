package com.wex.purchasetransaction.auth.repository.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String password, UserRole role) {
        this();
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
