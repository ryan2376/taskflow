package com.taskflow.api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Inspects the Authorization header on every request and, when it carries a valid
 * JWT, marks the request as authenticated for the current user.
 *
 * <p>Extends {@link OncePerRequestFilter} so it executes exactly once per request
 * (some servlet setups can otherwise invoke filters multiple times).
 *
 * <p>It is intentionally LENIENT: if there's no token or the token is bad, it simply
 * does nothing and lets the chain continue. It is NOT this filter's job to reject the
 * request — that's the job of Spring Security's authorization rules (SecurityConfig),
 * which will return 401 when they find no authentication in the context.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader(AUTH_HEADER);

        // No bearer token? Nothing to do — continue and let authorization rules decide.
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(BEARER_PREFIX.length());

        // Only set authentication if the token is valid AND we haven't already
        // authenticated this request (defensive: avoids clobbering an existing context).
        if (jwtService.isTokenValid(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UUID userId = jwtService.extractUserId(token);

            // Build an Authentication whose PRINCIPAL is the user's UUID. Downstream
            // code (services in later phases) reads this to enforce "you can only
            // touch your own data". Authorities are empty — this app has no roles.
            // Using the 3-arg constructor marks the token as authenticated.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Storing it here is what makes the request "logged in" for the rest of
            // the filter chain, the authorization checks, and the controllers.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
