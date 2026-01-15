package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Unread notifications count response")
public class UnreadCountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Number of unread notifications", example = "5")
    @JsonProperty("unread_count")
    private Long unreadCount;
}
