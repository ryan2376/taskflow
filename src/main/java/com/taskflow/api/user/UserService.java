package com.taskflow.api.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Business operations on users.
 *
 * <p>{@code @Service} marks this as a Spring-managed service bean (component-scanned
 * and injectable). Right now it just fetches the current user; later phases lean on
 * the user identity it resolves.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by id, or fail with 404 if absent.
     *
     * <p>{@code @Transactional(readOnly = true)} runs this in a read-only transaction —
     * a hint that lets the database/JDBC driver optimise (no dirty-checking, no flush).
     */
    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
