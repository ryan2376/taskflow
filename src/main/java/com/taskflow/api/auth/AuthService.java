package com.taskflow.api.auth;

import com.taskflow.api.auth.dto.AuthResponse;
import com.taskflow.api.auth.dto.LoginRequest;
import com.taskflow.api.auth.dto.RegisterRequest;
import com.taskflow.api.user.User;
import com.taskflow.api.user.UserRepository;
import com.taskflow.api.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles registration and login: the only places that create users or issue tokens.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Create a new account. We normalise the email to lowercase (so "A@x.com" and
     * "a@x.com" are the same identity), reject duplicates, BCrypt-hash the password,
     * save, and immediately issue a token so the client is logged in after signing up.
     *
     * <p>{@code @Transactional} (read-write): the existence check + insert run in one
     * transaction; any thrown exception rolls it back.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))   // store the HASH, never the password
                .displayName(request.displayName().trim())
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.bearer(token, UserResponse.from(user));
    }

    /**
     * Verify credentials and issue a token.
     *
     * <p>SECURITY: when the email is unknown OR the password is wrong, we return the
     * exact same 401 with an identical message. Revealing "no such email" vs "wrong
     * password" would let an attacker discover which emails are registered.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.bearer(token, UserResponse.from(user));
    }
}
