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
@Schema(description = "Comment detail response")
public class CommentDetailResponse {

    @Schema(description = "Comment information")
    private CommentResponse comment;

    @Schema(description = "Message", example = "Your comment has been published")
    private String message;
}

