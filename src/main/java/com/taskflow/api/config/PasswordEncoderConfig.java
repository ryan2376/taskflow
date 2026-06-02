package com.taskflow.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the application's {@link PasswordEncoder} bean.
 *
 * <p>We expose it as a standalone bean (rather than inlining it in SecurityConfig)
 * so it can be injected anywhere — the registration service will call
 * {@code encode(rawPassword)} to hash on sign-up, and the login flow will call
 * {@code matches(rawPassword, storedHash)} to verify.
 *
 * <p>{@link BCryptPasswordEncoder} uses the BCrypt algorithm with a default
 * "strength" (work factor) of 10. Each call to {@code encode} generates a fresh
 * random salt and embeds it in the result, so the same password hashes differently
 * every time — yet {@code matches} still works because the salt travels inside the
 * stored hash. We never manage salts ourselves.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 10 = 2^10 key-expansion rounds. Higher = slower = harder to brute
        // force. 10 is a sensible default; bump it as hardware improves.
        return new BCryptPasswordEncoder();
    }
}
