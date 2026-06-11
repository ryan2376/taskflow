package com.taskflow.api.task.repository;

import com.taskflow.api.task.entity.Task;
import com.taskflow.api.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
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
     * All of one user's tasks as a plain list. Used by Phase 7's simple GET /tasks.
     * (Phase 8 will switch the endpoint to the Specification + Pageable variants below
     * for filtering and pagination.)
     */
    List<Task> findByOwnerId(UUID ownerId);

    /**
     * A page of one user's tasks. {@link Pageable} carries the page number, size, and
     * sort; {@link Page} returns the rows plus total counts for pagination metadata.
     * (Phase 8 layers filtering on top of this via Specifications.)
     */
    Page<Task> findByOwnerId(UUID ownerId, Pageable pageable);

    // ---- Phase 10: analytics aggregation — the COUNTING happens in the database ----

    /** Total number of the owner's tasks. */
    long countByOwnerId(UUID ownerId);

    /** Number of the owner's tasks in a given status (DONE → "completed"). */
    long countByOwnerIdAndStatus(UUID ownerId, TaskStatus status);

    /**
     * "Overdue" = NOT in the given status (DONE) AND past its due date. Spring derives:
     * {@code WHERE owner_id = ? AND status <> ? AND due_date < ?}.
     */
    long countByOwnerIdAndStatusNotAndDueDateBefore(UUID ownerId, TaskStatus status, Instant time);

    /**
     * Tasks grouped by priority. This JPQL uses {@code GROUP BY}, so the DB returns just
     * one row per priority — each row is {@code [Priority, Long count]}.
     */
    @Query("select t.priority, count(t) from Task t where t.owner.id = :ownerId group by t.priority")
    List<Object[]> countByPriority(@Param("ownerId") UUID ownerId);

    /**
     * Tasks grouped by category name. The {@code left join} keeps tasks that have NO
     * category (their name comes back null → we label them "Uncategorized" in the service).
     */
    @Query("select c.name, count(t) from Task t left join t.category c where t.owner.id = :ownerId group by c.name")
    List<Object[]> countByCategory(@Param("ownerId") UUID ownerId);
}
