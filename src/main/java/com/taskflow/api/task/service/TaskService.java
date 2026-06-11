package com.taskflow.api.task.service;

import com.taskflow.api.category.entity.Category;
import com.taskflow.api.category.repository.CategoryRepository;
import com.taskflow.api.common.dto.PageResponse;
import com.taskflow.api.task.dto.TaskRequest;
import com.taskflow.api.task.dto.TaskResponse;
import com.taskflow.api.task.entity.Priority;
import com.taskflow.api.task.entity.Task;
import com.taskflow.api.task.entity.TaskStatus;
import com.taskflow.api.task.mapper.TaskMapper;
import com.taskflow.api.task.repository.TaskRepository;
import com.taskflow.api.task.spec.TaskSpecifications;
import com.taskflow.api.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/**
 * Business logic for tasks. Like CategoryService, every operation is scoped to the
 * authenticated owner, so users can only ever see and change their own tasks.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       CategoryRepository categoryRepository,
                       UserRepository userRepository,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    /**
     * List the owner's tasks — optionally filtered, always paginated.
     *
     * <p>We BUILD the query by composing Specification pieces: ownerEquals is mandatory;
     * each other filter is added only when its value was supplied (the helpers return
     * null otherwise, and Spring skips nulls when combining). {@code findAll(spec, pageable)}
     * comes from JpaSpecificationExecutor — it runs the combined WHERE plus the page's
     * limit/offset/sort as ONE SQL query, and also reports the total row count for the
     * pagination metadata.
     */
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> search(UUID ownerId,
                                             TaskStatus status,
                                             Priority priority,
                                             UUID categoryId,
                                             Instant dueBefore,
                                             String search,
                                             Pageable pageable) {
        // allOf(...) AND-combines every piece into one Specification, ignoring the nulls
        // (the filters the caller didn't supply). ownerEquals is the only one always present.
        Specification<Task> spec = Specification.allOf(
                TaskSpecifications.ownerEquals(ownerId),
                TaskSpecifications.statusEquals(status),
                TaskSpecifications.priorityEquals(priority),
                TaskSpecifications.categoryEquals(categoryId),
                TaskSpecifications.dueBefore(dueBefore),
                TaskSpecifications.textContains(search));

        // findAll returns a Page<Task>; .map converts each row to a DTO while preserving
        // the page metadata. Mapping runs inside this read-only transaction, so the lazy
        // category on each task loads cleanly.
        Page<TaskResponse> page = taskRepository.findAll(spec, pageable)
                .map(taskMapper::toResponse);

        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public TaskResponse getOne(UUID ownerId, UUID id) {
        return taskMapper.toResponse(getOwnedOrThrow(ownerId, id));
    }

    @Transactional
    public TaskResponse create(UUID ownerId, TaskRequest request) {
        // Default the optional enums when the client omits them.
        TaskStatus status = request.status() != null ? request.status() : TaskStatus.TODO;
        Priority priority = request.priority() != null ? request.priority() : Priority.MEDIUM;

        Task task = Task.builder()
                .title(request.title().trim())
                .description(request.description())
                .priority(priority)
                .dueDate(request.dueDate())
                .owner(userRepository.getReferenceById(ownerId))
                .category(resolveCategory(ownerId, request.categoryId()))
                .build();

        // Apply status through the helper so the completedAt invariant holds even if a
        // task is created directly as DONE.
        applyStatus(task, status);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(UUID ownerId, UUID id, TaskRequest request) {
        Task task = getOwnedOrThrow(ownerId, id);

        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
        task.setDueDate(request.dueDate());
        task.setCategory(resolveCategory(ownerId, request.categoryId()));
        applyStatus(task, request.status() != null ? request.status() : TaskStatus.TODO);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    /**
     * PATCH /tasks/{id}/complete — toggle done-state.
     * If the task is DONE, revert it to TODO; otherwise mark it DONE. The completedAt
     * timestamp is kept in sync by applyStatus.
     */
    @Transactional
    public TaskResponse toggleComplete(UUID ownerId, UUID id) {
        Task task = getOwnedOrThrow(ownerId, id);
        TaskStatus next = (task.getStatus() == TaskStatus.DONE) ? TaskStatus.TODO : TaskStatus.DONE;
        applyStatus(task, next);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(UUID ownerId, UUID id) {
        Task task = getOwnedOrThrow(ownerId, id);
        taskRepository.delete(task);
    }

    // ---- helpers ----

    /**
     * Maintains the invariant: completedAt is non-null IF AND ONLY IF status == DONE.
     * Setting DONE stamps completedAt (unless already set); any other status clears it.
     */
    private void applyStatus(Task task, TaskStatus status) {
        task.setStatus(status);
        if (status == TaskStatus.DONE) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    /**
     * Resolve an optional categoryId to one of the caller's categories. Returns null if
     * no category was supplied; rejects (400) a category that doesn't exist or isn't the
     * caller's — you can't file a task under someone else's category.
     */
    private Category resolveCategory(UUID ownerId, UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findByIdAndOwnerId(categoryId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "category not found"));
    }

    /** The ownership gate — 404 if the task doesn't exist or isn't the caller's. */
    private Task getOwnedOrThrow(UUID ownerId, UUID id) {
        return taskRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
