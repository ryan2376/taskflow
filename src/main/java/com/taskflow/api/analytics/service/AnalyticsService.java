package com.taskflow.api.analytics.service;

import com.taskflow.api.analytics.dto.AnalyticsSummary;
import com.taskflow.api.task.entity.Priority;
import com.taskflow.api.task.entity.TaskStatus;
import com.taskflow.api.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Computes the per-user analytics summary.
 *
 * <p>KEY DESIGN POINT (from the brief): every number comes from a database aggregation
 * query — COUNTs and GROUP BYs run by Postgres. We never load the user's tasks into a
 * list and tally them in Java. The only loops here iterate over the tiny grouped results
 * (at most 3 priorities, a handful of categories), just to reshape them into maps.
 */
@Service
public class AnalyticsService {

    private final TaskRepository taskRepository;

    public AnalyticsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummary summary(UUID ownerId) {
        long total = taskRepository.countByOwnerId(ownerId);
        long completed = taskRepository.countByOwnerIdAndStatus(ownerId, TaskStatus.DONE);
        long pending = total - completed;
        long overdue = taskRepository.countByOwnerIdAndStatusNotAndDueDateBefore(
                ownerId, TaskStatus.DONE, Instant.now());

        // completionRate as a fraction 0.0–1.0, rounded to 2 decimals; guard divide-by-zero.
        double completionRate = (total == 0)
                ? 0.0
                : Math.round(((double) completed / total) * 100.0) / 100.0;

        // Seed all priorities at 0 so the response always has LOW/MEDIUM/HIGH keys,
        // then overlay the actual counts from the grouped query.
        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            byPriority.put(p.name(), 0L);
        }
        for (Object[] row : taskRepository.countByPriority(ownerId)) {
            byPriority.put(((Priority) row[0]).name(), (Long) row[1]);
        }

        // Category counts; null name (no category) becomes "Uncategorized".
        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Object[] row : taskRepository.countByCategory(ownerId)) {
            String name = (row[0] == null) ? "Uncategorized" : (String) row[0];
            byCategory.put(name, (Long) row[1]);
        }

        return new AnalyticsSummary(total, completed, pending, overdue, completionRate, byPriority, byCategory);
    }
}
