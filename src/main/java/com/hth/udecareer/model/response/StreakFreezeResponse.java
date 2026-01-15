package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Streak freeze response")
public class StreakFreezeResponse {

    @JsonProperty("freeze_used")
    private Boolean freezeUsed;

    @JsonProperty("remaining_freezes")
    private Integer remainingFreezes;

    private String message;
}
