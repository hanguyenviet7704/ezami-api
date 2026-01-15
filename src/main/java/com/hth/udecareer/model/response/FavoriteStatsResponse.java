package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hth.udecareer.enums.FavoritableType;
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
@Schema(description = "Thống kê favorites của user")
public class FavoriteStatsResponse {

    @Schema(description = "Tổng số favorites", example = "50")
    private long totalFavorites;

    @Schema(description = "Số course được favorite", example = "20")
    private long totalCourses;

    @Schema(description = "Số lesson được favorite", example = "15")
    private long totalLessons;

    @Schema(description = "Số quiz được favorite", example = "10")
    private long totalQuizzes;

    @Schema(description = "Số topic được favorite", example = "3")
    private long totalTopics;

    @Schema(description = "Số post được favorite", example = "2")
    private long totalPosts;
}
