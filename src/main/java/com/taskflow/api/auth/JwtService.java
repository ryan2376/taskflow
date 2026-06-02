package com.taskflow.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Mints and validates JSON Web Tokens (JWTs).
 *
 * <p>This service has no state beyond the signing key and expiry, both injected from
 * configuration (app.jwt.secret / app.jwt.expiration-ms). The dev profile supplies a
 * default secret; production REQUIRES the JWT_SECRET env var (no fallback).
 */
@Service
public class JwtService {

    /** The HMAC signing key, derived once from the configured secret. */
    private final SecretKey signingKey;

    /** How long a freshly issued token stays valid, in milliseconds. */
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Keys.hmacShaKeyFor validates that the secret is long enough (>= 256 bits)
        // for HMAC signing and throws a WeakKeyException if it isn't — a guardrail
        // against insecure short secrets. Our secret is read as UTF-8 bytes.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Build a signed token for a user. The subject ("sub" claim) is the user's UUID —
     * the stable identity we'll read back on every authenticated request. We also
     * stash the email as a convenience claim.
     */
    public String generateToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);
        return Jwts.builder()
                .subject(userId.toString())          // "sub"
                .claim("email", email)               // custom claim
                .issuedAt(Date.from(now))            // "iat"
                .expiration(Date.from(expiry))       // "exp" — JJWT rejects expired tokens on parse
                .signWith(signingKey)                // appends the HMAC signature
                .compact();                          // serialise to the header.payload.signature string
    }

    /** Extract the user's UUID from a token's subject. Assumes the token is already validated. */
    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    /**
     * Returns true only if the token's signature is valid AND it hasn't expired.
     * parseSignedClaims throws if the signature is wrong, the token is malformed, or
     * it has expired — we translate any such failure into a simple false.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parse + verify the token in one step. verifyWith(signingKey) checks the HMAC
     * signature; parseSignedClaims also enforces the expiration. Returns the payload
     * (claims) on success, throws on any problem.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
