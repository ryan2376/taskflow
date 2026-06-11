package com.taskflow.api.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A tidy, framework-agnostic shape for paginated responses.
 *
 * <p>Spring Data hands us a {@link Page}, but its default JSON form is large and tied to
 * Spring's internals. Instead we expose exactly the fields a frontend needs to render
 * "Page 2 of 47" and Prev/Next buttons. {@code <T>} is a TYPE PARAMETER (a generic): this
 * one wrapper works for a page of tasks, categories, anything — {@code PageResponse<TaskResponse>}.
 *
 * @param content       the rows on THIS page (already mapped to DTOs)
 * @param page          current page number, 0-based (page 0 is the first page)
 * @param size          page size that was requested
 * @param totalElements total number of matching rows across ALL pages
 * @param totalPages    how many pages exist in total
 * @param first         is this the first page?
 * @param last          is this the last page?
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    /**
     * Convert a Spring Data {@link Page} (already containing DTOs) into our wrapper.
     * Keeping this mapping in one place means every paginated endpoint looks identical.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
