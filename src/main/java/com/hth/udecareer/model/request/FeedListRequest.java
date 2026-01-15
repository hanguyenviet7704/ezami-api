package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request parameters for listing feeds")
public class FeedListRequest {

    @Schema(description = "Page number (default: 1)", example = "1")
    private Integer page = 1;

    @Schema(description = "Items per page (default: 10)", example = "10")
    private Integer perPage = 10;

    @Schema(description = "Space slug to filter feeds", example = "start-here")
    private String space;

    @Schema(description = "User ID to filter feeds", example = "1")
    private Long userId;

    @Schema(description = "Search keyword", example = "đẹp")
    private String search;

    @Schema(description = "Fields to search in (default: ['post_content'])", example = "['post_content']")
    private List<String> searchIn;

    @Schema(description = "Order by type", example = "")
    private String orderByType;

    @Schema(description = "Disable sticky posts ('yes' or '')", example = "")
    private String disableSticky;
}

