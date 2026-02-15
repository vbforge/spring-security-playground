package com.vbforge.security.restjwt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * This filter runs on EVERY request and:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token
 * 3. Sets authentication in SecurityContext
 * 
 * Flow:
 * Request → Filter → Extract Token → Validate → Set Auth → Controller
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");
        
        // 2. Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request headers");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extract token (remove "Bearer " prefix)
            final String jwt = authHeader.substring(7);
            
            // 4. Extract username from token
            final String username = jwtUtil.extractUsername(jwt);

            // 5. If username exists and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 6. Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 7. Validate token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    log.info("JWT token validated successfully for user: {}", username);
                    
                    // 8. Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // 9. Set additional details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 10. Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("SecurityContext updated with authentication for user: {}", username);
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // 11. Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}