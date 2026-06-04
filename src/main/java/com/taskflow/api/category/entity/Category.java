package com.taskflow.api.category.entity;

import com.taskflow.api.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.UUID;

/**
 * A user-defined grouping for tasks (e.g. "Work", "Personal").
 *
 * <p>Introduces our first ENTITY RELATIONSHIP — the link to the owning {@link User}.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    /** Optional display colour, e.g. a hex code like "#3B82F6". Nullable in the schema. */
    @Column
    private String color;

    /**
     * The owner of this category. This is the Java side of the foreign key.
     *
     * <ul>
     *   <li>{@code @ManyToOne} — MANY categories relate to ONE user. JPA models the
     *       "many" side as a direct object reference ({@code getOwner()} returns a
     *       real User), instead of you juggling a raw UUID.</li>
     *   <li>{@code optional = false} — the relationship is mandatory (owner_id is
     *       NOT NULL), so Hibernate knows this can never be null.</li>
     *   <li>{@code fetch = FetchType.LAZY} — IMPORTANT. The default for @ManyToOne is
     *       EAGER, meaning every time we load a Category, Hibernate would also fire a
     *       query to load its User — even when we don't need it. LAZY defers that
     *       load until {@code getOwner()} is actually called, avoiding wasted queries
     *       (a common source of the dreaded "N+1 query" performance problem).</li>
     *   <li>{@code @JoinColumn(name = "owner_id")} — names the foreign-key column in
     *       THIS table that stores the referenced user's id, matching V1__init.sql.</li>
     * </ul>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
