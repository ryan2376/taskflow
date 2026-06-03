package com.taskflow.api.category;

import com.taskflow.api.category.dto.CategoryRequest;
import com.taskflow.api.category.dto.CategoryResponse;
import com.taskflow.api.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for categories. ALL methods are scoped to an ownerId (the
 * authenticated user). This is where the "you can only touch your own data" rule
 * lives — enforced in the service, not just the controller, so there's no way to
 * bypass it.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(UUID ownerId) {
        return categoryRepository.findByOwnerId(ownerId).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getOne(UUID ownerId, UUID id) {
        return categoryMapper.toResponse(getOwnedOrThrow(ownerId, id));
    }

    @Transactional
    public CategoryResponse create(UUID ownerId, CategoryRequest request) {
        Category category = Category.builder()
                .name(request.name().trim())
                .color(request.color())
                // getReferenceById returns a lazy User proxy WITHOUT a DB query — we only
                // need its id to populate the owner_id foreign key on insert.
                .owner(userRepository.getReferenceById(ownerId))
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID ownerId, UUID id, CategoryRequest request) {
        Category category = getOwnedOrThrow(ownerId, id);
        category.setName(request.name().trim());
        category.setColor(request.color());
        // Because 'category' is a managed entity inside this transaction, JPA's
        // dirty-checking would flush these changes on commit even without save();
        // we call save() anyway for readability.
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID ownerId, UUID id) {
        Category category = getOwnedOrThrow(ownerId, id);
        categoryRepository.delete(category);
    }

    /**
     * The single ownership gate. Returns the category only if it exists AND belongs to
     * ownerId; otherwise 404. We use 404 (not 403) on purpose: a "403 Forbidden" would
     * confirm the category exists but isn't yours, leaking information. 404 reveals
     * nothing — to this user, someone else's category simply does not exist.
     */
    private Category getOwnedOrThrow(UUID ownerId, UUID id) {
        return categoryRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }
}
