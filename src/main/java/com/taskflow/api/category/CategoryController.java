package com.taskflow.api.category;

import com.taskflow.api.category.dto.CategoryRequest;
import com.taskflow.api.category.dto.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * CRUD endpoints for categories. This controller is PROTECTED (not in the public
 * list), so every request already carries a validated JWT, and the authenticated
 * user's id arrives via {@code @AuthenticationPrincipal}. We pass that id into every
 * service call so ownership is always enforced.
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** GET /api/v1/categories — list MY categories. */
    @GetMapping
    public List<CategoryResponse> list(@AuthenticationPrincipal UUID userId) {
        return categoryService.getAll(userId);
    }

    /** POST /api/v1/categories — create one. 201 Created. */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** GET /api/v1/categories/{id} — one of MINE (404 if missing or not mine). */
    @GetMapping("/{id}")
    public CategoryResponse get(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return categoryService.getOne(userId, id);
    }

    /** PUT /api/v1/categories/{id} — update one of MINE. */
    @PutMapping("/{id}")
    public CategoryResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(userId, id, request);
    }

    /** DELETE /api/v1/categories/{id} — delete one of MINE. 204 No Content. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        categoryService.delete(userId, id);
    }
}
