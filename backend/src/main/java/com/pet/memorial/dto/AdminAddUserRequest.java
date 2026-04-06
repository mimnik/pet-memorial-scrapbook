package com.pet.memorial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AdminAddUserRequest {

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ROLE_USER|ROLE_ADMIN)$", message = "角色不合法")
    private String role;

    private String username;

    private String email;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
