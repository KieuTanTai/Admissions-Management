package com.example.admissions_management.application.dto.response;

import com.example.admissions_management.domain.model.UserRole;

public class UserAccountResponse {

    private Long id;
    private String username;
    private String fullName;
    private UserRole role;
    private Boolean enabled;

    public UserAccountResponse() {
    }

    public UserAccountResponse(Long id, String username, String fullName, UserRole role, Boolean enabled) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
