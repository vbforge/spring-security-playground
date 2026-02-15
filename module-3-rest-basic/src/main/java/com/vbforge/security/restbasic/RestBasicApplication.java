package com.vbforge.security.restbasic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Module 3 - REST + Basic Auth (Stateless)
 *
 * This module demonstrates STATELESS REST API security:
 *
 * 1. HTTP Basic Authentication
 *    - Credentials sent in Authorization header
 *    - Base64 encoded: Authorization: Basic base64(username:password)
 *    - Every request must include credentials
 *
 * 2. STATELESS Session Management
 *    - SessionCreationPolicy.STATELESS
 *    - NO sessions created on server
 *    - NO JSESSIONID cookie
 *    - Every request authenticated independently
 *
 * 3. CSRF Disabled
 *    - Not needed for stateless APIs
 *    - State-changing operations don't use cookies
 *
 * 4. Custom JSON Error Responses
 *    - 401 Unauthorized (no/invalid credentials)
 *    - 403 Forbidden (valid credentials, wrong role)
 *    - Proper REST API error format
 *
 * 5. Role-Based Access Control
 *    - USER role: Can access /api/products/**, /api/tags/**
 *    - ADMIN role: Can access /api/admin/** + everything USER can
 *
 * 6. In-Memory Users
 *    - user / password (USER role)
 *    - admin / admin (ADMIN role)
 *
 * Key Difference from Module 2:
 * - Module 2: Stateful (sessions, cookies, form login)
 * - Module 3: Stateless (no sessions, HTTP Basic, every request authenticated)
 *
 * This is the standard approach for REST APIs consumed by:
 * - Mobile applications
 * - Single Page Applications (SPAs)
 * - Microservices
 * - Third-party integrations
 *
 * Test Credentials:
 * - Regular User: user / password
 * - Admin User: admin / admin
 *
 * @author VBForge Security Team
 */
@SpringBootApplication
public class RestBasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestBasicApplication.class, args);

        System.out.println("\n" +
                "=================================================================\n" +
                "  Module 3 - REST + Basic Auth (Stateless) is running!\n" +
                "=================================================================\n" +
                "  🌐 Base URL:        http://localhost:8083\n" +
                "  📡 API Products:    http://localhost:8083/api/products\n" +
                "  📡 API Tags:        http://localhost:8083/api/tags\n" +
                "  👑 Admin API:       http://localhost:8083/api/admin/...\n" +
                "  📚 Swagger UI:      http://localhost:8083/swagger-ui.html\n" +
                "\n" +
                "  ⚡ Mode: STATELESS (no sessions, no cookies)\n" +
                "  🔐 Auth: HTTP Basic (every request)\n" +
                "  🚫 CSRF: Disabled (not needed)\n" +
                "\n" +
                "  Test Credentials:\n" +
                "  📝 Regular User:    user / password\n" +
                "  👑 Admin User:      admin / admin\n" +
                "\n" +
                "  Example cURL:\n" +
                "  curl -u user:password http://localhost:8083/api/products\n" +
                "=================================================================\n");
    }
}
