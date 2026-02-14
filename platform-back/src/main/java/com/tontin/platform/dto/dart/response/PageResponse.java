package com.tontin.platform.dto.dart.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic paginated response wrapper for API endpoints.
 *
 * @param <T> the type of content in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response containing a list of items and pagination metadata")
public class PageResponse<T> {

    @Schema(description = "List of items in the current page", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> content;

    @Schema(description = "Current page number (0-based)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private int page;

    @Schema(description = "Number of items per page", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private int size;

    @Schema(description = "Total number of elements across all pages", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private long totalElements;

    @Schema(description = "Total number of pages", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Whether there are more pages after this one", example = "true")
    private boolean hasNext;

    @Schema(description = "Whether there are pages before this one", example = "false")
    private boolean hasPrevious;

    /**
     * Creates a PageResponse from Spring Data Page object.
     *
     * @param page Spring Data Page object
     * @param <T> the type of content
     * @return PageResponse containing the page data
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
