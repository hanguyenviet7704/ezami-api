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
@Schema(description = "Space information")
public class SpaceInfoResponse {

    @Schema(description = "Space ID", example = "2")
    private Long id;

    @Schema(description = "Space title", example = "Start Here")
    private String title;

    @Schema(description = "Space slug", example = "start-here")
    private String slug;

    @Schema(description = "Space type", example = "community")
    private String type;
}

