package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response kiểm tra trạng thái favorite")
public class FavoriteCheckResponse {

    @JsonProperty("isFavorited")
    @Schema(description = "Item có được favorite hay không", example = "true")
    private boolean isFavorited;

    @Schema(description = "ID của favorite (nếu có)", example = "123")
    private Long favoriteId;
}
