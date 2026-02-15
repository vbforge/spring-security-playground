package com.vbforge.security.restjwt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin-only REST Controller
 * 
 * All endpoints require ADMIN role.
 * Used to demonstrate 403 Forbidden for USER role.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    /**
     * Get system statistics (ADMIN only)
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        log.info("Admin user '{}' accessing system stats", authentication.getName());

        Map<String, Object> stats = new HashMap<>();
        stats.put("timestamp", LocalDateTime.now());
        stats.put("admin", authentication.getName());
        stats.put("totalProducts", 0); // Placeholder
        stats.put("totalTags", 0);     // Placeholder
        stats.put("totalUsers", 2);    // We have 2 in-memory users
        stats.put("message", "Admin-only statistics endpoint");

        return ResponseEntity.ok(stats);
    }

    /**
     * Get server info (ADMIN only)
     * GET /api/admin/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo(Authentication authentication) {
        log.info("Admin user '{}' accessing server info", authentication.getName());

        Map<String, Object> info = new HashMap<>();
        info.put("timestamp", LocalDateTime.now());
        info.put("admin", authentication.getName());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        info.put("message", "Server information - ADMIN only");

        return ResponseEntity.ok(info);
    }
}