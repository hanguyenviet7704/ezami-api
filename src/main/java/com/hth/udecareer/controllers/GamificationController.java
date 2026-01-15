package com.hth.udecareer.controllers;


import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.enums.LeaderBoardType;
import com.hth.udecareer.model.response.LeaderBoardWithUserPageResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.PointHistoryResponse;
import com.hth.udecareer.model.response.UserPointsResponse;
import com.hth.udecareer.service.UserPointsService;
import com.hth.udecareer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Gamification", description = "APIs tính điểm và Bảng xếp hạng")
public class GamificationController {

    private final UserService userService;
    private final UserPointsService userPointsService;

    @Operation(
            summary = "Lấy bảng xếp hạng điểm (có phân trang)",
            description = """
        Trả về danh sách người dùng có điểm cao nhất theo loại bảng xếp hạng với phân trang.

        Tham số type:
        - WEEK: Bảng xếp hạng tuần hiện tại (mặc định)
        - MONTH: Bảng xếp hạng tháng hiện tại
        - YEAR: Bảng xếp hạng năm hiện tại

        Tham số phân trang:
        - page: Số trang (bắt đầu từ 0, mặc định: 0)
        - size: Số lượng kết quả mỗi trang (mặc định: 10, tối đa: 100)

        Điểm tuần/tháng/năm được tự động reset khi sang chu kỳ mới.
        Người đứng rank = 1 có thể dùng để hiển thị vương miện trên giao diện.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy leaderboard thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LeaderBoardWithUserPageResponse.class)))
    })

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/gamification/leaderboard")
    public ResponseEntity<LeaderBoardWithUserPageResponse> leaderboard(
            @RequestParam(defaultValue = "WEEK") LeaderBoardType type,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @Optional Principal principal) {
        String email = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(userService.getLeaderboardWithPagination(type, email, page, size));
    }



    @Operation(
            summary = "Lấy điểm của user hiện tại",
            description = """
                    **Lấy thông tin điểm của user đang đăng nhập**
                    
                    Trả về các loại điểm:
                    - current_points: Tổng điểm hiện tại (tất cả thời gian)
                    - week_points: Điểm trong tuần hiện tại
                    - month_points: Điểm trong tháng hiện tại
                    - year_points: Điểm trong năm hiện tại
                    
                    Điểm tuần/tháng/năm được tự động reset khi sang chu kỳ mới.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy điểm thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserPointsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/gamification/points")
    public ResponseEntity<UserPointsResponse> getUserPoints(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(userPointsService.getUserPoints(email));
    }



    @Operation(
            summary = "Lấy điểm và rank của user theo username",
            description = """
                    **Lấy thông tin điểm và xếp hạng của user theo username**
                    
                    Trả về thông tin:
                    - Điểm hiện tại: Tổng điểm tích lũy (current_points)
                    - Điểm tuần: Điểm trong tuần hiện tại (week_points)
                    - Điểm tháng: Điểm trong tháng hiện tại (month_points)
                    - Điểm năm: Điểm trong năm hiện tại (year_points)
                    - Rank tuần: Xếp hạng trong bảng xếp hạng tuần (week_rank)
                    - Rank tháng: Xếp hạng trong bảng xếp hạng tháng (month_rank)
                    - Rank năm: Xếp hạng trong bảng xếp hạng năm (year_rank)
                    
                    Điểm và rank được tự động reset khi sang chu kỳ mới.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy điểm và rank thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.UserPointsWithRankResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/gamification/points/by-username")
    public ResponseEntity<com.hth.udecareer.model.response.UserPointsWithRankResponse> getUserPointsWithRankByUsername(
            @RequestParam String username) {
        return ResponseEntity.ok(userPointsService.getUserPointsWithRank(username));
    }

    @Operation(
            summary = "Lấy lịch sử cộng điểm của user hiện tại",
            description = """
                    **Lấy lịch sử các lần cộng điểm của user đang đăng nhập**
                    
                    Trả về danh sách các hoạt động đã được cộng điểm, bao gồm:
                    - Loại hành động (đăng bài, comment, like, referral, etc.)
                    - Số điểm được cộng
                    - Thời gian cộng điểm
                    - Thông tin liên quan (feedId, relatedId nếu có)
                    
                    Tham số phân trang:
                    - page: Số trang (bắt đầu từ 0, mặc định: 0)
                    - size: Số lượng kết quả mỗi trang (mặc định: 20, tối đa: 100)
                    
                    Kết quả được sắp xếp theo thời gian giảm dần (mới nhất trước).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy lịch sử điểm thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/gamification/points/history")
    public ResponseEntity<PageResponse<PointHistoryResponse>> getPointHistory(
            Principal principal,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        String email = principal.getName();
        return ResponseEntity.ok(userPointsService.getUserPointHistory(email, page, size));
    }

}
