package com.vbforge.security.restjwt.controller;

import com.vbforge.security.restjwt.dto.AuthResponse;
import com.vbforge.security.restjwt.dto.LoginRequest;
import com.vbforge.security.restjwt.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication Controller
 * 
 * Handles login and JWT token generation.
 * 
 * Endpoints:
 * - POST /auth/login - Authenticate user and return JWT token
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Login endpoint
     * 
     * Flow:
     * 1. Receive username and password
     * 2. Authenticate using AuthenticationManager
     * 3. Generate JWT token
     * 4. Return token to client
     * 
     * POST /auth/login
     * Body: { "username": "user", "password": "password" }
     * Response: { "token": "eyJhbGc...", "username": "user", "roles": ["ROLE_USER"] }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        try {
            // 1. Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 3. Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            // 4. Extract roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            log.info("Login successful for user: {}", loginRequest.getUsername());

            // 5. Return response with token
            AuthResponse response = new AuthResponse(
                    token,
                    userDetails.getUsername(),
                    roles,
                    jwtExpiration
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {} - Invalid credentials", loginRequest.getUsername());
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Invalid username or password")
            );
        }
    }

    /**
     * Get current user info (requires authentication)
     * 
     * GET /auth/me
     * Header: Authorization: Bearer <token>
     * Response: { "username": "user", "roles": ["ROLE_USER"] }
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Not authenticated")
            );
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new UserInfoResponse(
                userDetails.getUsername(),
                roles
        ));
    }

    // Inner classes for responses
    record ErrorResponse(String message) {}
    record UserInfoResponse(String username, List<String> roles) {}
}