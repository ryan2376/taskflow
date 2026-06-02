package com.taskflow.api.common.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Switches on Spring Data JPA's "auditing" feature application-wide.
 *
 * <p>What auditing does: when an entity has fields annotated {@code @CreatedDate}
 * and {@code @LastModifiedDate} (and the entity is registered with
 * {@code @EntityListeners(AuditingEntityListener.class)}), Spring automatically
 * fills those fields in for us — {@code @CreatedDate} on the first insert,
 * {@code @LastModifiedDate} on every insert and update. We never write
 * "task.setUpdatedAt(now())" by hand; it's handled by a JPA lifecycle callback
 * that fires just before the row is written.
 *
 * <p>Why a separate {@code @Configuration} class instead of putting
 * {@code @EnableJpaAuditing} on the main application class? Keeping it isolated
 * makes it easy to exclude in certain test slices (auditing needs the JPA
 * infrastructure present), and it documents the feature in one obvious place.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // No body needed — the annotation does all the work by registering the
    // AuditingEntityListener infrastructure into the Spring context.
}
