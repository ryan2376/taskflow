package com.taskflow.api.task.mapper;

import com.taskflow.api.category.dto.CategoryResponse;
import com.taskflow.api.category.entity.Category;
import com.taskflow.api.category.mapper.CategoryMapper;
import com.taskflow.api.task.dto.TaskResponse;
import com.taskflow.api.task.entity.Task;
import org.springframework.stereotype.Component;

/**
 * Maps Task entities to their DTOs.
 *
 * <p>Demonstrates mapper composition: rather than duplicate category-mapping logic,
 * we inject {@link CategoryMapper} and delegate. This is the payoff of having a
 * dedicated mapper per resource.
 *
 * <p>Note: this runs inside the service's transaction, so touching the lazy
 * {@code task.getCategory()} safely triggers its load when present.
 */
@Component
public class TaskMapper {

    private final CategoryMapper categoryMapper;

    public TaskMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public TaskResponse toResponse(Task task) {
        Category category = task.getCategory();
        CategoryResponse categoryResponse = (category == null) ? null : categoryMapper.toResponse(category);

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCompletedAt(),
                categoryResponse,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
