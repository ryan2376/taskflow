package com.taskflow.api.task.entity;

import com.taskflow.api.category.entity.Category;
import com.taskflow.api.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * The core domain object: a single to-do item owned by a {@link User} and
 * optionally filed under a {@link Category}.
 */
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)   // enables the @CreatedDate/@LastModifiedDate below
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    /**
     * Free-form notes. columnDefinition = "text" maps to the unbounded TEXT column
     * in our schema (rather than the default VARCHAR(255)).
     */
    @Column(columnDefinition = "text")
    private String description;

    /**
     * {@code @Enumerated(EnumType.STRING)} stores the enum's NAME ("TODO") as text,
     * not its ordinal number. Always prefer STRING for enums you persist — ordinals
     * break the moment someone reorders the enum constants. length = 20 lines up with
     * the VARCHAR(20) column.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    /**
     * When the task is due. Stored as TIMESTAMPTZ. We use {@link Instant} (an absolute
     * point on the UTC timeline) so there's no timezone ambiguity — Hibernate maps
     * Instant to a "timestamp with time zone" column.
     */
    @Column(name = "due_date")
    private Instant dueDate;

    /** Set when the task is marked DONE; null otherwise. */
    @Column(name = "completed_at")
    private Instant completedAt;

    /** Mandatory owner. Deleting a user cascades to their tasks (enforced by the DB FK). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Optional category. optional = true (the default) because category_id is
     * nullable — a task can be uncategorised. If its category is deleted, the DB sets
     * this column to NULL (ON DELETE SET NULL), so the task survives uncategorised.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Auto-set once on insert by the auditing listener. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Auto-updated on every insert AND update by the auditing listener. */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
