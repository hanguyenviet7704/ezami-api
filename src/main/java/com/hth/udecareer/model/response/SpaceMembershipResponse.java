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
@Schema(description = "Space membership response")
public class SpaceMembershipResponse {

    @Schema(description = "Space ID", example = "1")
    private Long spaceId;

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "Is member", example = "true")
    private Boolean isMember;

    @Schema(description = "Role in space", example = "member")
    private String role;

    @Schema(description = "Message", example = "Successfully joined space")
    private String message;
}
