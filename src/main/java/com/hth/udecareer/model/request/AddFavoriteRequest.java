package com.hth.udecareer.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để thêm một item vào danh sách yêu thích")
public class AddFavoriteRequest {

    @NotNull(message = "Favoritable ID is required")
    @Schema(description = "ID của nội dung muốn yêu thích (từ wp_posts.ID)", example = "12345", required = true)
    private Long favoritableId;
}
