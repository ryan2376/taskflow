package com.taskflow.api.task;

/**
 * The lifecycle state of a {@link Task}.
 *
 * <p>These names are stored in the database as plain text (via
 * {@code @Enumerated(EnumType.STRING)} on the Task entity), so they MUST exactly
 * match the values allowed by the {@code chk_tasks_status} CHECK constraint in
 * V1__init.sql: TODO, IN_PROGRESS, DONE. If the two ever drift apart, an insert
 * will be rejected by the database — a good safety net.
 *
 * <p>We store the enum's NAME (not its ordinal position) because names are stable:
 * reordering or inserting a value later won't silently corrupt existing rows, which
 * is exactly what would happen if we stored ordinals (0,1,2...).
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
