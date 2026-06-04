package com.taskflow.api.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * A registered user — the owner of categories and tasks.
 *
 * <p>This is our first JPA entity, so here's the annotation tour:
 *
 * <ul>
 *   <li>{@code @Entity} — marks this class as a JPA entity: Hibernate will map it
 *       to a database table and manage its lifecycle (persist/find/update/remove).</li>
 *   <li>{@code @Table(name = "users")} — the table to map to. We MUST name it
 *       explicitly because Hibernate would otherwise derive "user", which is a
 *       reserved word in PostgreSQL (and matches our V1 migration's table name).</li>
 *   <li>{@code @EntityListeners(AuditingEntityListener.class)} — hooks this entity
 *       into the auditing machinery we enabled in JpaAuditingConfig, so the
 *       {@code @CreatedDate} field below is filled automatically.</li>
 * </ul>
 *
 * <p>The Lombok annotations generate boilerplate at compile time:
 * <ul>
 *   <li>{@code @Getter}/{@code @Setter} — generate get/set methods for every field.</li>
 *   <li>{@code @NoArgsConstructor} — a no-argument constructor, which JPA REQUIRES
 *       to instantiate entities when loading rows from the DB.</li>
 *   <li>{@code @AllArgsConstructor} + {@code @Builder} — convenient construction in
 *       our own code, e.g. {@code User.builder().email(...).build()}.</li>
 * </ul>
 * Note we deliberately do NOT use Lombok's {@code @Data} on entities: it generates
 * equals/hashCode/toString over ALL fields, which on entities causes subtle bugs
 * (lazy-loading triggers, infinite recursion across relationships, broken hashCode
 * before the ID is assigned). Plain getters/setters are the safe choice.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Primary key. {@code @Id} marks it as the PK; {@code @GeneratedValue} with
     * strategy UUID tells Hibernate to generate a random UUID before insert
     * (application-side), matching the UUID column type in our schema. We choose
     * app-side generation so we know the ID immediately, without a DB round-trip.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Login identity. unique=true documents the intent and helps Hibernate's schema
     * validation line up with our uq_users_email constraint. nullable=false maps to
     * the NOT NULL column.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /** The BCrypt hash of the password — NEVER the raw password. Maps to password_hash. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Human-friendly name shown in the UI. Maps to display_name. */
    @Column(name = "display_name", nullable = false)
    private String displayName;

    /**
     * When the row was first created. {@code @CreatedDate} is set ONCE by the
     * auditing listener on insert. {@code updatable = false} guarantees we never
     * accidentally overwrite it on later updates.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
