package com.cbcportal.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Controls whether the user is allowed to log in
    @Column(nullable = false)
    private boolean isApproved = false;

    // One-time code emailed to the user to confirm their identity at login
    private String loginCode;

    private LocalDateTime loginCodeExpiry;

    // Wrong verification attempts against the current loginCode; code is invalidated past the limit
    @Column(nullable = false)
    private int loginCodeAttempts = 0;

    // Default Constructor required by JPA
    public User() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    @JsonIgnore
    public String getLoginCode() { return loginCode; }
    public void setLoginCode(String loginCode) { this.loginCode = loginCode; }

    @JsonIgnore
    public LocalDateTime getLoginCodeExpiry() { return loginCodeExpiry; }
    public void setLoginCodeExpiry(LocalDateTime loginCodeExpiry) { this.loginCodeExpiry = loginCodeExpiry; }

    @JsonIgnore
    public int getLoginCodeAttempts() { return loginCodeAttempts; }
    public void setLoginCodeAttempts(int loginCodeAttempts) { this.loginCodeAttempts = loginCodeAttempts; }
}