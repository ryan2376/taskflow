package com.taskflow.api.category;

import com.taskflow.api.category.dto.CategoryResponse;
import org.springframework.stereotype.Component;

/**
 * Translates between Category entities and their DTOs.
 *
 * <p>A {@code @Component} so it can be injected into the service. Keeping mapping here
 * (rather than inline in the service) means there's a single, testable place that
 * decides what a Category looks like on the wire.
 */
@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor()
        );
    }
}
