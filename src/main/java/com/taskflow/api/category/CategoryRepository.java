package com.taskflow.api.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data-access interface for {@link Category} rows.
 *
 * <p>Note how Spring Data traverses the {@code owner} relationship: in a method
 * name, {@code OwnerId} means "the id of the owner association" → it generates
 * {@code WHERE owner_id = ?}. You get association-aware queries for free.
 */
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /** All categories belonging to one user — used to list "my categories". */
    List<Category> findByOwnerId(UUID ownerId);

    /**
     * Fetch a single category ONLY if it belongs to the given owner. This pattern
     * (id + ownerId together) is how we'll enforce ownership in the service layer:
     * if the result is empty, either the category doesn't exist OR it isn't yours —
     * and we treat both the same way, so users can't probe for others' data.
     */
    Optional<Category> findByIdAndOwnerId(UUID id, UUID ownerId);
}
