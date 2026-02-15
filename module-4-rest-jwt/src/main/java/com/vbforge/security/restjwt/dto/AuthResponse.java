package com.vbforge.security.restjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Authentication Response DTO
 * 
 * Returned by /auth/login endpoint after successful authentication.
 * Contains JWT token and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private List<String> roles;
    private Long expiresIn; // Expiration time in milliseconds

    public AuthResponse(String token, String username, List<String> roles, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expiresIn = expiresIn;
    }
}