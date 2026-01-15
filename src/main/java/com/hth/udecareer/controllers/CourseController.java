package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.CourseNoteRequest;
import com.hth.udecareer.model.request.PaginationRequest;
import com.hth.udecareer.model.response.*;
import com.hth.udecareer.service.CourseService;
import com.hth.udecareer.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Course Management")
public class CourseController {

    private final CourseService courseService;
    private final LessonService lessonService;


    @Operation(
            summary = "Lấy thông tin chi tiết khóa học",
            description = "Trả về thông tin chi tiết của một khóa học, bao gồm danh sách bài học."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin khóa học thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học", content = @Content),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/courses/{courseId}")
    public CourseResponse getCourseInfo(
            @Parameter(description = "ID của khóa học", required = true, example = "74000")
            @PathVariable Long courseId, Principal principal) {

        return courseService.getCourseInfo(courseId, principal.getName());
    }

//    @Operation(
//            summary = "Lấy danh sách khóa học (không phân trang)",
//            description = "Trả về danh sách khóa học theo bộ lọc truyền vào qua query params. Nếu không truyền tham số, trả về tất cả khóa học."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Lấy danh sách khóa học thành công"),
//            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
//            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
//    })
//    @SecurityRequirement(name = "bearerAuth")
//    @GetMapping("/courses") // without pagination
//    public List<CoursePreResponse> getCourses(
//            @Parameter(description = "Các tham số lọc (ví dụ: post_title, post_status)", required = false)
//            @RequestParam Map<String, Object> params) {
//
//        return courseService.getCourses(params);
//    }

    @Operation(
            summary = "Lấy danh sách khóa học có phân trang (WEB)",
            description = """
                    Trả về danh sách khóa học có phân trang và sort.
                    Tham số query hỗ trợ: page (0-based), size, sort (field,direction) và các filter khác.
                    Response chứa metadata phân trang: page, size, totalElements, totalPages, hasNext, hasPrevious.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách khóa học phân trang thành công"),
            @ApiResponse(responseCode = "400", description = "Tham số phân trang/lọc không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/courses-pagination")
    public PageResponse<CoursePreResponse> getCoursesUsingPagination(
            @Parameter(description = "Số trang (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "20")
            @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Chuỗi sắp xếp, ví dụ: 'id,desc'", example = "id,desc")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Các tham số lọc bổ sung", required = false)
            @RequestParam(required = false) Map<String, Object> params,
            @Nullable Principal principal) {

        params.remove("page");
        params.remove("size");
        params.remove("sort");

        PaginationRequest paginationRequest = PaginationRequest.of(page, size, sort);

        String email = principal != null ? principal.getName() : null;

        Page<CoursePreResponse> result =
                courseService.getCoursesUsingPagination(params, paginationRequest.toPageable(), email);

        return PageResponse.of(result);
    }

    @Operation(
            summary = "Lấy chi tiết bài học trong khóa học",
            description = "Trả về nội dung chi tiết của một bài học thuộc khóa học (courseId + lessonId)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết bài học thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học hoặc bài học", content = @Content),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public LessonDetailResponse getLesson(
            @Parameter(description = "ID của khóa học", required = true, example = "74000")
            @PathVariable Long courseId,
            @Parameter(description = "ID của bài học", required = true, example = "75005")
            @PathVariable Long lessonId,
            Principal principal) {
        return lessonService.getLesson(courseId, lessonId, principal.getName());
    }


    @Operation(
            summary = "Tạo hoặc cập nhật ghi chú cho khóa học",
            description = """
                    **Tạo mới hoặc cập nhật ghi chú (note) của user cho một khóa học cụ thể.**
                    
                    - Mỗi user chỉ có tối đa 1 note cho mỗi khóa học.
                    - Nếu đã tồn tại, API sẽ cập nhật nội dung note hiện tại.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo/cập nhật ghi chú khóa học thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu ghi chú không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/courses/{courseId}/note")
    public CourseNoteResponse createOrUpdateCourseNote(@PathVariable Long courseId,
                                                       @RequestBody CourseNoteRequest request,
                                                       Principal principal) {
        return courseService.createOrUpdateCourseNote(courseId, request, principal);
    }

    @Operation(
            summary = "Lấy ghi chú của user cho khóa học",
            description = """
                    **Lấy nội dung ghi chú (note) mà user đã lưu cho một khóa học.**
                    
                    - Nếu user chưa có note cho khóa học này, có thể trả về nội dung rỗng hoặc null.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy ghi chú khóa học thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khóa học hoặc ghi chú", content = @Content),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/courses/{courseId}/note")
    public CourseNoteResponse getCourseNoteResponse(@PathVariable Long courseId,
                                                    Principal principal) {
        return courseService.getCourseNoteResponse(courseId, principal);
    }
}
