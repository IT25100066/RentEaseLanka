package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filter that intercepts every HTTP request to extract and validate the JWT.
 * It ensures only valid token holders access protected endpoints.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Extract the JWT token from the incoming HTTP request header
            String jwt = getJwtFromRequest(request);

            // 2. Validate the token using JwtProvider
            if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
                // 3. Extract user identity (email) from the validated token
                String email = jwtProvider.getEmailFromToken(jwt);

                // 4. Create an authentication object for Spring Security's context
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Store authentication in the security context (User is now logged in for this request)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // Continue request execution down the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to parse the 'Authorization: Bearer <token>' header.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer " prefix
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix and return only the raw token string
        }
        return null;
    }
}