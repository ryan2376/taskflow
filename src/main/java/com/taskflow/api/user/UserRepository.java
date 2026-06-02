package com.taskflow.api.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data-access interface for {@link User} rows.
 *
 * <p>This is the magic of Spring Data JPA: we write an INTERFACE and never
 * implement it. At startup Spring generates a proxy bean that implements every
 * method for us.
 *
 * <p>By extending {@code JpaRepository<User, UUID>} (entity type = User, id type =
 * UUID) we instantly get a full set of methods: {@code save}, {@code findById},
 * {@code findAll}, {@code delete}, {@code count}, paginated/sorted finders, etc.
 *
 * <p>The two methods below are DERIVED QUERIES. Spring parses the method NAME and
 * writes the SQL automatically — no annotations, no JPQL:
 * <ul>
 *   <li>{@code findByEmail(String)} → {@code SELECT * FROM users WHERE email = ?}.
 *       Returning {@link Optional} expresses "there may be no such user" without
 *       risking a null.</li>
 *   <li>{@code existsByEmail(String)} → an efficient existence check (used in Phase 5
 *       to reject duplicate registrations without loading the whole row).</li>
 * </ul>
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
