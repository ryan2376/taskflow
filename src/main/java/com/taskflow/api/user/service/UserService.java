package com.taskflow.api.user.service;

import com.taskflow.api.common.exception.NotFoundException;
import com.taskflow.api.user.entity.User;
import com.taskflow.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
