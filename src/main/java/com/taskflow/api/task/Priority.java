package com.taskflow.api.task;

/**
 * The importance level of a {@link Task}.
 *
 * <p>Like {@link TaskStatus}, these are stored as text and must match the
 * {@code chk_tasks_priority} CHECK constraint in V1__init.sql: LOW, MEDIUM, HIGH.
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}
