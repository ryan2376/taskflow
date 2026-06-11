package com.taskflow.api.analytics.controller;

import com.taskflow.api.analytics.dto.AnalyticsSummary;
import com.taskflow.api.analytics.service.AnalyticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Read-only analytics for the authenticated user. Protected (needs a token); the summary
 * is always scoped to the caller's own tasks via {@code @AuthenticationPrincipal}.
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /** GET /api/v1/analytics/summary — totals, completion rate, and breakdowns for MY tasks. */
    @GetMapping("/summary")
    public AnalyticsSummary summary(@AuthenticationPrincipal UUID userId) {
        return analyticsService.summary(userId);
    }
}
