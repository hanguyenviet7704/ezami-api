package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comment list response with pagination")
public class CommentListResponse {

    @Schema(description = "Comments with pagination")
    private PageResponse<CommentResponse> comments;
}
