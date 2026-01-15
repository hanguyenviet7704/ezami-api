package com.hth.udecareer.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Lịch sử làm bài quiz kèm thống kê tổng hợp")
public class QuizHistoryWrapperResponse {

    @Schema(description = "Thống kê tổng hợp")
    private QuizStatistics statistics;

    @Schema(description = "Danh sách lịch sử làm bài với phân trang")
    private PageResponse<QuizHistoryResponse> history;
}
