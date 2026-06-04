package com.taskflow.api.config;

import com.taskflow.api.auth.filter.JwtAuthFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * The central security policy for the whole API.
 *
 * <p>{@code @EnableWebSecurity} switches on Spring Security's web support and lets us
 * define our own {@link SecurityFilterChain}, REPLACING Boot's default (which locked
 * everything behind a generated password).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Paths anyone may hit without a token. */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",          // register & login (added in Phase 5)
            "/v3/api-docs/**",          // OpenAPI spec
            "/swagger-ui/**",           // Swagger UI assets
            "/swagger-ui.html",         // Swagger UI entry point
            "/actuator/health",         // liveness check
            "/actuator/info"            // build/info
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection guards browser form/cookie sessions. We're a stateless
            // token API (no cookies, no sessions), so CSRF doesn't apply — disable it.
            .csrf(csrf -> csrf.disable())

            // Apply the cross-origin rules from CorsConfig.
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // The authorization rules, evaluated top-down.
            .authorizeHttpRequests(auth -> auth
                    // Permit internal ERROR/FORWARD dispatches so the container can
                    // render error responses (404s, and Phase 9's JSON error bodies).
                    // These dispatch types can't be triggered by an external caller,
                    // so allowing them is safe — only REQUEST dispatches come from outside.
                    .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                    .requestMatchers(PUBLIC_PATHS).permitAll()
                    .anyRequest().authenticated())   // everything else needs a valid token

            // STATELESS: never create or use an HttpSession. Each request must carry
            // its own JWT; the server remembers nothing between requests.
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // When an unauthenticated caller hits a protected path, return 401
            // (not the default 403). Phase 9 will give this a JSON body via the
            // global exception handler; for now a bare 401 status is enough.
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))

            // We use neither browser login form nor HTTP Basic — only bearer tokens.
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())

            // Run our JWT filter BEFORE the username/password filter, so by the time
            // authorization is checked, the SecurityContext is already populated.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
