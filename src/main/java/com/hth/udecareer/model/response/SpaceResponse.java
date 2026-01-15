package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Space (Không gian) response")
public class SpaceResponse {
    
    @Schema(description = "Space ID", example = "1")
    private Long id;
    
    @Schema(description = "Tiêu đề space", example = "Bắt đầu tại đây")
    private String title;
    
    @Schema(description = "Slug của space (dùng để filter feeds)", example = "start-here")
    private String slug;
    
    @Schema(description = "Loại space", example = "community")
    private String type;
    
    @Schema(description = "Quyền riêng tư", example = "public")
    private String privacy;
    
    @Schema(description = "Danh sách spaces con (nested structure)")
    private List<SpaceResponse> spaces = new ArrayList<>();

    private Boolean isJoined;
}
