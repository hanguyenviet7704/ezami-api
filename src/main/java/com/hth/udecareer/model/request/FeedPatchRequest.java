package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to patch (partial update) a feed")
public class FeedPatchRequest {

    @Schema(description = "Is sticky (0 or 1) - Đánh dấu bài viết sticky (luôn hiển thị đầu tiên)", example = "1")
    private Integer isSticky;
}

