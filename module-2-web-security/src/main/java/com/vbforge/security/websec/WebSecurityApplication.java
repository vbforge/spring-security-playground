package com.vbforge.security.websec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Module 2 - Web Security
 *
 * This module demonstrates traditional web application security with:
 *
 * 1. Form-Based Login
 *    - Custom login page built with Thymeleaf
 *    - Username/password authentication
 *    - Remember-me functionality
 *
 * 2. Session-Based Authentication (Stateful)
 *    - Server stores authentication state
 *    - JSESSIONID cookie for subsequent requests
 *    - Session management and invalidation
 *
 * 3. In-Memory User Storage
 *    - Two test users configured in WebSecurityConfig
 *    - user/password (USER role)
 *    - admin/admin (ADMIN role)
 *
 * 4. Role-Based Access Control
 *    - Public pages: /, /home, /login
 *    - User pages: /products, /api/**
 *    - Admin pages: /admin/**
 *
 * 5. CSRF Protection
 *    - Enabled for all state-changing operations
 *    - Automatic token handling in Thymeleaf forms
 *
 * 6. Custom Error Pages
 *    - 403 Access Denied page
 *    - Logout success page
 *
 * Test Credentials:
 * - Regular User: user / password
 * - Admin User: admin / admin
 *
 * @author VBForge Security Team
 */
@SpringBootApplication
public class WebSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebSecurityApplication.class, args);

        System.out.println("\n" +
                "=================================================================\n" +
                "  Module 2 - Web Security is running!\n" +
                "=================================================================\n" +
                "  🌐 Application URL: http://localhost:8082\n" +
                "  🔐 Login Page:      http://localhost:8082/login\n" +
                "  🛍️  Products Page:   http://localhost:8082/products\n" +
                "  👑 Admin Page:      http://localhost:8082/admin\n" +
                "\n" +
                "  Test Credentials:\n" +
                "  📝 Regular User:    user / password\n" +
                "  👑 Admin User:      admin / admin\n" +
                "=================================================================\n");
    }
}
