package com.taskflow.api.task.spec;

import com.taskflow.api.task.entity.Priority;
import com.taskflow.api.task.entity.Task;
import com.taskflow.api.task.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

/**
 * A toolbox of {@link Specification} "filter pieces" for querying tasks.
 *
 * <p>WHAT IS A SPECIFICATION? It's a small object representing ONE condition of a SQL
 * {@code WHERE} clause — e.g. "status = DONE". Each method below builds one such piece.
 * In the service we snap together only the pieces the caller actually asked for (with
 * {@code .and(...)}) and hand the combined rule to the repository. Hibernate then turns
 * the whole thing into a single SQL query.
 *
 * <p>WHY BOTHER? Without this we'd need a separate repository method for every possible
 * combination of filters ({@code findByOwnerAndStatus}, {@code ...AndPriority},
 * {@code ...AndCategory}, ...). That count explodes. Specifications let us compose
 * filters dynamically at runtime instead.
 *
 * <p>THE NULL TRICK: each optional filter returns {@code null} when the caller didn't
 * supply that value. Spring Data treats a {@code null} Specification as "no restriction"
 * and simply skips it when combining — so absent filters quietly drop out.
 *
 * <p>The three lambda parameters {@code (root, query, cb)} come from the Criteria API:
 * <ul>
 *   <li>{@code root} — the entity we're querying ({@code Task}); {@code root.get("title")}
 *       refers to the {@code title} column.</li>
 *   <li>{@code query} — the overall query (unused here, hence ignored).</li>
 *   <li>{@code cb} — the CriteriaBuilder, a factory for conditions like
 *       {@code cb.equal(...)}, {@code cb.like(...)}, {@code cb.lessThan(...)}.</li>
 * </ul>
 *
 * <p>This is a utility class (only static methods), so the constructor is private to
 * stop anyone instantiating it.
 */
public final class TaskSpecifications {

    private TaskSpecifications() { }

    /**
     * ALWAYS applied: restrict to the owner's rows. This is the ownership rule expressed
     * as a Specification, so it can never be forgotten when filtering.
     * {@code root.get("owner").get("id")} walks the owner relationship down to its id —
     * SQL: {@code WHERE owner_id = ?}.
     */
    public static Specification<Task> ownerEquals(UUID ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    /** Optional: status = ? (skipped when status is null). */
    public static Specification<Task> statusEquals(TaskStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Optional: priority = ? */
    public static Specification<Task> priorityEquals(Priority priority) {
        if (priority == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    /**
     * Optional: belongs to this category. Walks the (nullable) category relationship to
     * its id. Note this naturally EXCLUDES uncategorised tasks — which is what you want
     * when you ask "show me tasks in category X".
     */
    public static Specification<Task> categoryEquals(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /** Optional: due strictly before the given instant. Tasks with no due date are excluded. */
    public static Specification<Task> dueBefore(Instant dueBefore) {
        if (dueBefore == null) {
            return null;
        }
        // The <Instant> hint types the path so cb.lessThan accepts it (it needs a Comparable).
        return (root, query, cb) -> cb.lessThan(root.<Instant>get("dueDate"), dueBefore);
    }

    /**
     * Optional free-text search: case-insensitive "contains" against title OR description.
     * {@code cb.lower(...)} + a lowercased pattern makes the match case-insensitive;
     * the {@code %term%} wildcards mean "appears anywhere".
     */
    public static Specification<Task> textContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.<String>get("title")), pattern),
                cb.like(cb.lower(root.<String>get("description")), pattern)
        );
    }
}
