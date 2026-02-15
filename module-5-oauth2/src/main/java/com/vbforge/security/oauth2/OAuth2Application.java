package com.vbforge.security.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Module 5 - OAuth2 Resource Server
 *
 * This module demonstrates OAuth2 Resource Server with external identity provider:
 *
 * 1. OAuth2 Resource Server
 *    - Validates JWT tokens from Keycloak
 *    - Extracts user info and roles from token
 *    - No local authentication (delegated to Keycloak)
 *
 * 2. External Identity Provider (Keycloak)
 *    - Handles user authentication
 *    - Issues JWT tokens
 *    - Manages users, roles, permissions
 *    - Provides SSO (Single Sign-On)
 *
 * 3. JWT Token Validation
 *    - Spring auto-validates signature with Keycloak's public key
 *    - Checks token expiration
 *    - Extracts claims (username, roles, etc.)
 *
 * 4. Role Mapping
 *    - Maps Keycloak realm roles to Spring Security authorities
 *    - Keycloak: realm_access.roles = ["user", "admin"]
 *    - Spring: ROLE_USER, ROLE_ADMIN
 *
 * 5. STATELESS Session Management
 *    - No sessions on resource server
 *    - Every request validated independently
 *    - Perfect for microservices
 *
 * Authentication Flow:
 * ┌────────┐              ┌──────────┐              ┌────────────────┐
 * │ Client │              │ Keycloak │              │ Resource Server│
 * └───┬────┘              └────┬─────┘              └────────┬───────┘
 *     │                        │                             │
 *     │ 1. Login Request       │                             │
 *     │ ──────────────────────>│                             │
 *     │                        │ 2. Authenticate user        │
 *     │                        │ 3. Generate JWT             │
 *     │ 4. Return JWT          │                             │
 *     │ <────────────────────── │                             │
 *     │                        │                             │
 *     │ 5. API Request + JWT   │                             │
 *     │ ────────────────────────────────────────────────────>│
 *     │                        │                             │ 6. Validate JWT
 *     │                        │ 7. Request public key       │    with Keycloak
 *     │                        │ <───────────────────────────│
 *     │                        │ 8. Return public key        │
 *     │                        │ ────────────────────────────>│
 *     │                        │                             │ 9. Verify signature
 *     │                        │                             │ 10. Extract roles
 *     │ 11. API Response       │                             │
 *     │ <────────────────────────────────────────────────────│
 *
 * Key Differences from Module 4 (Our JWT):
 *
 * | Feature | Module 4 | Module 5 |
 * |---------|----------|----------|
 * | Token Generation | Our app | Keycloak |
 * | User Management | In-memory | Keycloak |
 * | Token Validation | Our JwtUtil | Spring OAuth2 |
 * | Public Key | Our secret | Keycloak's public key |
 * | Login Endpoint | /auth/login | Keycloak URL |
 * | User Database | In-memory | Keycloak's DB |
 * | SSO Support | No | Yes |
 * | Social Login | No | Yes (configurable) |
 * | Enterprise Ready | No | Yes |
 *
 * Keycloak Setup Required:
 * 1. Start Keycloak (docker-compose up -d)
 * 2. Create realm: spring-security-playground
 * 3. Create client: spring-security-playground-client
 * 4. Create roles: user, admin
 * 5. Create users with roles
 *
 * See KEYCLOAK-SETUP.md for detailed instructions.
 *
 * Test Flow:
 * 1. Get token from Keycloak:
 *    curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
 *      -d "client_id=spring-security-playground-client" \
 *      -d "client_secret=YOUR_SECRET" \
 *      -d "grant_type=password" \
 *      -d "username=user" \
 *      -d "password=password"
 *
 * 2. Use token with API:
 *    curl http://localhost:8085/api/products \
 *      -H "Authorization: Bearer <token>"
 *
 * @author VBForge Security Team
 */
@SpringBootApplication
public class OAuth2Application {

    public static void main(String[] args) {
        SpringApplication.run(OAuth2Application.class, args);

        System.out.println("\n" +
                "=================================================================\n" +
                "  Module 5 - OAuth2 Resource Server is running!\n" +
                "=================================================================\n" +
                "  🌐 Resource Server:  http://localhost:8085\n" +
                "  🔐 Keycloak:         http://localhost:8080\n" +
                "  📡 API Products:     http://localhost:8085/api/products\n" +
                "  📡 API Tags:         http://localhost:8085/api/tags\n" +
                "  👑 Admin API:        http://localhost:8085/api/admin/stats\n" +
                "  📚 Swagger UI:       http://localhost:8085/swagger-ui.html\n" +
                "\n" +
                "  ⚡ Mode: OAuth2 Resource Server (Stateless)\n" +
                "  🎫 Auth: JWT tokens from Keycloak\n" +
                "  🏢 Provider: Keycloak (external)\n" +
                "  🚫 CSRF: Disabled (not needed)\n" +
                "\n" +
                "  📋 Prerequisites:\n" +
                "  1. Keycloak must be running (docker-compose up -d)\n" +
                "  2. Realm 'spring-security-playground' configured\n" +
                "  3. Client 'spring-security-playground-client' created\n" +
                "  4. Users created: user/password, admin/admin\n" +
                "\n" +
                "  🔑 Get Token from Keycloak:\n" +
                "  curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \\\n" +
                "    -H \"Content-Type: application/x-www-form-urlencoded\" \\\n" +
                "    -d \"client_id=spring-security-playground-client\" \\\n" +
                "    -d \"client_secret=YOUR_CLIENT_SECRET\" \\\n" +
                "    -d \"grant_type=password\" \\\n" +
                "    -d \"username=user\" \\\n" +
                "    -d \"password=password\"\n" +
                "\n" +
                "  🚀 Use Token:\n" +
                "  curl http://localhost:8085/api/products \\\n" +
                "    -H \"Authorization: Bearer <your-token>\"\n" +
                "\n" +
                "  📖 See KEYCLOAK-SETUP.md for detailed setup instructions\n" +
                "=================================================================\n");
    }
}
