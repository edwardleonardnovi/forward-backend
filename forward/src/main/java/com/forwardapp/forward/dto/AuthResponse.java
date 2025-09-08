package com.forwardapp.forward.dto;

public class AuthResponse {
    private String token;
    private String role;

    public AuthResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
    public void setToken(String token) { this.token = token; }
    public void setRole(String role) { this.role = role; }
}
