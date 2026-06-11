package com.taskflow.api.task.controller;

import com.taskflow.api.common.dto.PageResponse;
import com.taskflow.api.task.dto.TaskRequest;
import com.taskflow.api.task.dto.TaskResponse;
import com.taskflow.api.task.entity.Priority;
import com.taskflow.api.task.entity.TaskStatus;
import com.taskflow.api.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * CRUD endpoints for tasks, plus the complete-toggle. Protected: the authenticated
 * user's id arrives via {@code @AuthenticationPrincipal} and is passed into every
 * service call for ownership enforcement.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * GET /api/v1/tasks — list MY tasks, with optional filters and pagination.
     *
     * <p>Every query parameter is optional; Spring binds each one automatically:
     * <ul>
     *   <li>{@code status}, {@code priority} — text like "DONE"/"HIGH" → the matching enum.</li>
     *   <li>{@code categoryId} — parsed into a UUID.</li>
     *   <li>{@code dueBefore} — an ISO-8601 instant such as {@code 2026-07-01T09:00:00Z};
     *       {@code @DateTimeFormat} tells Spring how to read it.</li>
     *   <li>{@code search} — free text matched against title/description.</li>
     *   <li>{@code page}, {@code size}, {@code sort} — Spring folds these into a
     *       {@link Pageable}. {@code @PageableDefault} supplies defaults (20 per page,
     *       newest first) when omitted. Example: {@code ?page=0&size=10&sort=dueDate,asc}.</li>
     * </ul>
     */
    @GetMapping
    public PageResponse<TaskResponse> list(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueBefore,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return taskService.search(userId, status, priority, categoryId, dueBefore, search, pageable);
    }

    /** POST /api/v1/tasks — create one. 201 Created. */
    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** GET /api/v1/tasks/{id} — one of MINE (404 otherwise). */
    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return taskService.getOne(userId, id);
    }

    /** PUT /api/v1/tasks/{id} — replace one of MINE. */
    @PutMapping("/{id}")
    public TaskResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request) {
        return taskService.update(userId, id, request);
    }

    /**
     * PATCH /api/v1/tasks/{id}/complete — toggle done-state.
     * PATCH (not PUT) because it's a partial, targeted state change rather than a full
     * replacement of the resource. No body needed.
     */
    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return taskService.toggleComplete(userId, id);
    }

    /** DELETE /api/v1/tasks/{id} — delete one of MINE. 204 No Content. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        taskService.delete(userId, id);
    }
}
