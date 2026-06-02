package com.taskflow.api.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Data-access interface for {@link Task} rows.
 *
 * <p>This repo extends a SECOND interface, {@code JpaSpecificationExecutor<Task>},
 * in addition to JpaRepository. That adds methods that accept a dynamic
 * {@code Specification} — the building block we'll use in Phase 8 to implement
 * flexible filtering (by status, priority, due date, search text) without writing a
 * separate query method for every combination. We wire it in now so the plumbing is
 * ready; we'll actually use it later.
 */
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    /**
     * One task, but only if it belongs to the given owner (ownership enforcement,
     * same pattern as categories).
     */
    Optional<Task> findByIdAndOwnerId(UUID id, UUID ownerId);

    /**
     * A page of one user's tasks. {@link Pageable} carries the page number, size, and
     * sort; {@link Page} returns the rows plus total counts for pagination metadata.
     * (Phase 8 layers filtering on top of this via Specifications.)
     */
    Page<Task> findByOwnerId(UUID ownerId, Pageable pageable);
}
