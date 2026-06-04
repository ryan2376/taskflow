package com.taskflow.api.task.service;

import com.taskflow.api.category.entity.Category;
import com.taskflow.api.category.repository.CategoryRepository;
import com.taskflow.api.task.dto.TaskRequest;
import com.taskflow.api.task.dto.TaskResponse;
import com.taskflow.api.task.entity.Priority;
import com.taskflow.api.task.entity.Task;
import com.taskflow.api.task.entity.TaskStatus;
import com.taskflow.api.task.mapper.TaskMapper;
import com.taskflow.api.task.repository.TaskRepository;
import com.taskflow.api.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll(UUID ownerId) {
        return taskRepository.findByOwnerId(ownerId).stream()
                .map(taskMapper::toResponse)
                .toList();
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
