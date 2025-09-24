package com.ankurshala.backend.dto.admin;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AdminProfileDto {
    private Long id;
    
    @Size(max = 20)
    private String phoneNumber;
    
    private Boolean isSuperAdmin = false;
    
    private LocalDateTime lastLogin;

    // Constructors
    public AdminProfileDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getIsSuperAdmin() {
        return isSuperAdmin;
    }

    public void setIsSuperAdmin(Boolean isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
