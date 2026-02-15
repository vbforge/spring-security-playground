package com.vbforge.security.defaultsec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Module 1 - Default Security
 *
 * This module demonstrates Spring Security auto-configuration.
 * When spring-boot-starter-security is added to dependencies,
 * Spring Boot automatically:
 *
 * 1. Enables HTTP Basic Authentication
 * 2. Protects all endpoints (requires authentication)
 * 3. Generates a default user with random password
 * 4. Enables CSRF protection
 * 5. Adds security headers
 * 6. Sets up a default security filter chain
 *
 * Default Credentials:
 * - Username: user
 * - Password: Look for "Using generated security password:" in console logs
 *
 * @author VBForge Security Team
 */

@SpringBootApplication
public class DefaultSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(DefaultSecurityApplication.class, args);
    }

}
