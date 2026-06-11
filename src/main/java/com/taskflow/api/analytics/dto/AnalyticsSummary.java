package com.taskflow.api.analytics.dto;

import java.util.Map;

/**
 * Dashboard summary for the authenticated user — the response of GET /api/v1/analytics/summary.
 *
 * @param total          total number of the user's tasks
 * @param completed      tasks with status DONE
 * @param pending        tasks not yet done (total - completed)
 * @param overdue        not-done tasks whose due date is in the past
 * @param completionRate fraction done, 0.0–1.0 (e.g. 0.6 means 60% complete)
 * @param byPriority     count per priority, e.g. {"LOW":1,"MEDIUM":2,"HIGH":2}
 * @param byCategory     count per category name, with uncategorised tasks under "Uncategorized"
 */
public record AnalyticsSummary(
        long total,
        long completed,
        long pending,
        long overdue,
        double completionRate,
        Map<String, Long> byPriority,
        Map<String, Long> byCategory
) {}
