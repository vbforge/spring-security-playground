package com.vbforge.security.restjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Module 4 - REST + JWT
 *
 * This module demonstrates JWT (JSON Web Token) authentication:
 *
 * 1. JWT Token-Based Authentication
 *    - Login endpoint returns JWT token
 *    - Token contains user info and roles
 *    - Token has expiration time
 *    - Client sends token in Authorization header: Bearer <token>
 *
 * 2. STATELESS Session Management
 *    - SessionCreationPolicy.STATELESS
 *    - NO sessions created on server
 *    - NO cookies
 *    - Every request validated by JWT token
 *
 * 3. JWT Authentication Filter
 *    - Intercepts every request
 *    - Extracts and validates JWT token
 *    - Sets authentication in SecurityContext
 *    - Runs before controller
 *
 * 4. Login Flow
 *    - POST /auth/login with credentials
 *    - Server validates and generates JWT
 *    - Client stores token (localStorage, memory, etc.)
 *    - Client includes token in subsequent requests
 *
 * 5. Token Structure
 *    Header:  { "alg": "HS256", "typ": "JWT" }
 *    Payload: { "sub": "username", "roles": [...], "iat": ..., "exp": ... }
 *    Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
 *
 * 6. Advantages over HTTP Basic (Module 3)
 *    - Credentials NOT sent with every request
 *    - Token contains user info (no DB lookup on every request)
 *    - Built-in expiration
 *    - More secure (credentials stored client-side only once)
 *
 * 7. Role-Based Access Control
 *    - USER role: Can access /api/products/**, /api/tags/**
 *    - ADMIN role: Can access /api/admin/** + everything USER can
 *
 * 8. Public Endpoints
 *    - POST /auth/login (get JWT token)
 *    - GET /auth/me (get current user info)
 *    - Swagger UI for API documentation
 *
 * Authentication Flow:
 * ┌──────┐                    ┌──────────┐
 * │Client│                    │  Server  │
 * └──┬───┘                    └────┬─────┘
 *    │ 1. POST /auth/login         │
 *    │    {user, pass}             │
 *    │  ─────────────────────────> │
 *    │                             │ 2. Validate
 *    │                             │ 3. Generate JWT
 *    │ 4. Return JWT token         │
 *    │ <─────────────────────────  │
 *    │                             │
 *    │ 5. GET /api/products        │
 *    │    Authorization: Bearer token
 *    │  ─────────────────────────> │
 *    │                             │ 6. Validate token
 *    │                             │ 7. Extract user info
 *    │ 8. Return data              │
 *    │ <─────────────────────────  │
 *
 * Test Credentials:
 * - Regular User: user / password
 * - Admin User: admin / admin
 *
 * @author VBForge Security Team
 */
@SpringBootApplication
public class RestJwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestJwtApplication.class, args);

        System.out.println("\n" +
                "=================================================================\n" +
                "  Module 4 - REST + JWT is running!\n" +
                "=================================================================\n" +
                "  🌐 Base URL:        http://localhost:8084\n" +
                "  🔐 Login:           POST http://localhost:8084/auth/login\n" +
                "  👤 Current User:    GET  http://localhost:8084/auth/me\n" +
                "  📡 API Products:    GET  http://localhost:8084/api/products\n" +
                "  📡 API Tags:        GET  http://localhost:8084/api/tags\n" +
                "  👑 Admin API:       GET  http://localhost:8084/api/admin/stats\n" +
                "  📚 Swagger UI:      http://localhost:8084/swagger-ui.html\n" +
                "\n" +
                "  ⚡ Mode: STATELESS with JWT tokens\n" +
                "  🎫 Auth: JWT Bearer token\n" +
                "  🚫 CSRF: Disabled (not needed)\n" +
                "  ⏰ Token Expiry: 24 hours\n" +
                "\n" +
                "  Test Credentials:\n" +
                "  📝 Regular User:    user / password\n" +
                "  👑 Admin User:      admin / admin\n" +
                "\n" +
                "  Example Flow:\n" +
                "  1. Login:\n" +
                "     curl -X POST http://localhost:8084/auth/login \\\n" +
                "       -H \"Content-Type: application/json\" \\\n" +
                "       -d '{\"username\":\"user\",\"password\":\"password\"}'\n" +
                "\n" +
                "  2. Use token:\n" +
                "     curl http://localhost:8084/api/products \\\n" +
                "       -H \"Authorization: Bearer <your-token>\"\n" +
                "=================================================================\n");
    }
}
