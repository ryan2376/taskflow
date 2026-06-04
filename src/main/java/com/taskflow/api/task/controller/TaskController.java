package com.taskflow.api.task.controller;

import com.taskflow.api.task.dto.TaskRequest;
import com.taskflow.api.task.dto.TaskResponse;
import com.taskflow.api.task.service.TaskService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    /** GET /api/v1/tasks — list MY tasks (no filtering yet; that's Phase 8). */
    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal UUID userId) {
        return taskService.getAll(userId);
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
