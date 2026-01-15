package com.hth.udecareer.model.response;

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
@Schema(description = "Reaction information")
public class ReactionResponse {

    @Schema(description = "Reaction ID", example = "2")
    private Long id;

    @Schema(description = "User ID", example = "1")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "Reaction type", example = "like")
    private String type;

    @Schema(description = "User profile")
    private XProfileResponse xprofile;
}

