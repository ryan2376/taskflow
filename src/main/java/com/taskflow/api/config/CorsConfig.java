package com.taskflow.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Defines which browser origins may call this API.
 *
 * <p>Allowed origins come from configuration (app.cors.allowed-origins, comma
 * separated). Locally that's the Next.js dev server at http://localhost:3000;
 * in production you'd set CORS_ALLOWED_ORIGINS to your deployed frontend's URL.
 *
 * <p>SecurityConfig references the {@link CorsConfigurationSource} bean below via
 * {@code http.cors(...)}, so these rules are applied by Spring Security's filter
 * chain (the correct place for CORS in a secured app).
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        // The HTTP verbs our API uses.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Allow any request header (notably Authorization, which carries our JWT).
        config.setAllowedHeaders(List.of("*"));
        // Let the browser read these response headers if we ever set them.
        config.setExposedHeaders(List.of("Authorization"));
        // Allow credentials (cookies/Authorization) on cross-origin calls.
        config.setAllowCredentials(true);

        // Apply this configuration to every path.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
