package com.hth.udecareer.controllers;

import java.security.Principal;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.ExplainAnswerRequest;
import com.hth.udecareer.model.request.PaginationRequest;
import com.hth.udecareer.model.request.SubmitAnswerRequest;
import com.hth.udecareer.model.response.CategoryResponse;
import com.hth.udecareer.model.response.ExplainAnswerResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.QuizInfoResponse;
import com.hth.udecareer.model.response.QuizResponse;
import com.hth.udecareer.model.response.SubmitAnswerResponse;
import com.hth.udecareer.service.QuestionService;
import com.hth.udecareer.service.QuizCategoryService;
import com.hth.udecareer.service.QuizMasterService;
import com.hth.udecareer.model.dto.QuizSearchRequestDto;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
@org.springframework.validation.annotation.Validated
@Tag(name = "Quiz Management")
public class QuizController {

        private final QuizMasterService quizMasterService;
        private final QuizCategoryService quizCategoryService;
        private final QuestionService questionService;


    @Operation(
            summary = "Tìm kiếm quiz",
            description = """
                    **Tìm kiếm danh sách quiz theo danh mục hoặc loại bài test(Mobile)**
                    
                    API này trả về danh sách các quiz/bài kiểm tra dựa trên các bộ lọc:
                    - **category**: Mã danh mục (vd: "toeic-reading", "toeic-listening")
                    - **typeTest**: Loại test (vd: "full", "mini", "part1", "part2")
                    
                    **Tham số:**
                    - Có thể truyền cả 2 tham số, 1 tham số, hoặc không tham số nào
                    - Không truyền tham số = lấy tất cả quiz
                    
                    **Response:**
                    - Danh sách quiz với thông tin cơ bản
                    - Bao gồm thống kê nếu user đã làm bài (answeredQuestions, percentage, pass, etc.)
                    
                    **Yêu cầu:**
                    - Phải có JWT token hợp lệ
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy danh sách quiz"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "default",
                    description = "Lỗi không mong đợi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @GetMapping("/quiz")
    public List<QuizResponse> searchQuiz(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Mã danh mục quiz (title) - tùy chọn. Nếu có categoryCode thì bỏ qua field này",
                    example = "PSM I",
                    required = false
            )
            @RequestParam(value = "category", required = false) String category,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Mã danh mục quiz (code) - tùy chọn. Nếu có thì ưu tiên dùng thay cho category",
                    example = "PSM-I",
                    required = false
            )
            @RequestParam(value = "categoryCode", required = false) String categoryCode,
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "Loại bài test (tùy chọn). Ví dụ: 'full' (full test), 'mini' (mini test), 'part1', 'part2', etc.",
                    example = "full",
                    required = false
            )
            @RequestParam(value = "typeTest", required = false) String typeTest)
            throws AppException {
        log.info("searchQuiz: user {}, category {}, categoryCode {}, typeTest {}", principal.getName(), category, categoryCode, typeTest);
        return quizMasterService.searchQuiz(principal.getName(), category, categoryCode, typeTest);
    }


    @Operation(
                summary = "Tìm kiếm quiz với phân trang và filter(WEB)",
                description = """
                        **Tìm kiếm danh sách quiz với nhiều bộ lọc và phân trang**

                        API này trả về danh sách các quiz/bài kiểm tra dựa trên các bộ lọc:
                        - **category**: Mã danh mục (vd: "toeic-reading", "toeic-listening")
                        - **quizType**: Loại quiz - "mini" (chỉ mini test), "full" (không mini), "all" (tất cả)
                        - **minTimeLimit**: Lọc quiz có thời gian >= giá trị này (phút)
                        - **maxTimeLimit**: Lọc quiz có thời gian <= giá trị này (phút)
                        - **page**: Số trang (0-based, mặc định 0)
                        - **size**: Kích thước trang (1-100, mặc định 20)
                        - **sort**: Sắp xếp theo trường (format: "field,direction", vd: "id,desc")

                        **Validation:**
                        - quizType phải là: mini, full, hoặc all
                        - minTimeLimit và maxTimeLimit phải là số dương
                        - minTimeLimit phải <= maxTimeLimit
                        - sort field phải trong danh sách: id, name, timeLimit, slug, postId, postTitle

                        **Response:**
                        - Danh sách quiz với thông tin cơ bản và phân trang
                        - Bao gồm thống kê nếu user đã làm bài (answeredQuestions, percentage, pass, etc.)
                        - Metadata: page, size, totalElements, totalPages, hasNext, hasPrevious

                        **Yêu cầu:**
                        - Phải có JWT token hợp lệ
                        """
        )
        @SecurityRequirement(name = "bearerAuth")
                        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Tìm thấy danh sách quiz"),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
                        @ApiResponse(responseCode = "default",
                                        description = "Lỗi không mong đợi",
                                        content = @Content(mediaType = "application/json",
                                                schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @GetMapping("/quiz/search")
        public PageResponse<QuizResponse> searchQuiz(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
                        @Valid
                        @ParameterObject
                        QuizSearchRequestDto request
                )throws AppException {
                    log.info("searchQuiz: user {}, request: {}", principal.getName(), request);
                    // Gọi service với các trường từ DTO
                    Page<QuizResponse> result = quizMasterService.searchQuizPaged(
                            principal.getName(),
                            request.getCategory(),
                            request.getCategoryCode(),
                            request.getQuizType(),
                            request.getMinTimeLimit(),
                            request.getMaxTimeLimit(),
                            request.getCourseId(),
                            PaginationRequest.of(request.getPage(), request.getSize(), request.getSort()).toPageable());

                    PageResponse<QuizResponse> pageResponse = PageResponse.of(result);
                    
                    // Tạo categoryInfo nếu có categoryCode và không phải "all"
                    if (request.getCategoryCode() != null && !request.getCategoryCode().isEmpty() 
                            && !"all".equalsIgnoreCase(request.getCategoryCode())) {
                        pageResponse.setCategoryInfo(quizMasterService.getCategoryInfo(
                                principal.getName(), request.getCategoryCode()));
                    }
                    
                    return pageResponse;
        }

        @Operation(
                summary = "Lấy tất cả danh mục quiz",
                description = """
                        **Lấy danh sách tất cả các danh mục quiz có sẵn**

                        API này trả về tất cả các danh mục quiz/bài kiểm tra trong hệ thống.

                        **Response bao gồm:**
                        - Thông tin danh mục (code, title, header, imageUri)
                        - Số lượng bài test trong mỗi danh mục (numFullTest, numMiniTest)
                        - Thông tin mua hàng của user (purchasedInfo)
                        - Mã offer/entitlement từ RevenueCat (nếu có)

                        **Tính năng tìm kiếm:**
                        - `title`: Tìm kiếm theo tên hoặc mã danh mục (tìm kiếm không phân biệt hoa thường)
                        - Có thể tìm theo title hoặc code của danh mục
                        - Để trống để lấy tất cả danh mục

                        **Hỗ trợ phân trang tùy chọn:**
                        - Nếu không truyền tham số `page` và `size`: trả về tất cả danh mục dưới dạng List<CategoryResponse>
                        - Nếu truyền tham số `page` hoặc `size`: trả về dữ liệu phân trang dưới dạng PageResponse<CategoryResponse>
                        
                        **Tham số phân trang:**
                        - `page`: Số trang (bắt đầu từ 0), mặc định = 0
                        - `size`: Số lượng item mỗi trang, mặc định = 20
                        - `sort`: Sắp xếp (ví dụ: "order,asc" hoặc "title,desc")

                        **Use cases:**
                        - Hiển thị danh sách danh mục trên trang chủ
                        - Tìm kiếm danh mục cụ thể
                        - Cho phép user chọn danh mục để xem quiz
                        - Hiển thị trạng thái mua/chưa mua của từng danh mục

                        **Yêu cầu:**
                        - Phải có JWT token hợp lệ
                        """
        )
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200",
                                description = "Lấy danh sách danh mục thành công. Response type phụ thuộc vào việc có sử dụng phân trang hay không: List<CategoryResponse> (không phân trang) hoặc PageResponse<CategoryResponse> (có phân trang)"),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
                        @ApiResponse(responseCode = "default",
                        description = "Lỗi không mong đợi",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @GetMapping("/quiz/category")
        public Object getAllCategory(
                @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Tìm kiếm theo tên danh mục hoặc mã danh mục - tùy chọn",
                    example = "TOEIC"
                )
                @RequestParam(required = false) String title,
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số trang (bắt đầu từ 0) - tùy chọn. Nếu có thì sẽ trả về phân trang",
                    example = "0"
                )
                @RequestParam(required = false) Integer page,
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số lượng item mỗi trang - tùy chọn. Nếu có thì sẽ trả về phân trang",
                    example = "20"
                )
                @RequestParam(required = false) Integer size,
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Sắp xếp theo trường (format: 'field,direction') - tùy chọn",
                    example = "order,asc"
                )
                @RequestParam(required = false) String sort) throws AppException {

                log.info("getAllCategory: user {}", principal.getName());

                // Nếu không có tham số phân trang, trả về danh sách không phân trang
                if (page == null && size == null) {
                        log.info("getAllCategory: no pagination requested");
                        return quizCategoryService.findAll(principal.getName(), title);
                }

                // Nếu có tham số phân trang, sử dụng phân trang
                log.info("getAllCategory: pagination requested - page: {}, size: {}, sort: {}",
                        page, size, sort);

                PaginationRequest request = PaginationRequest.of(page, size, sort);

                return PageResponse.of(quizCategoryService.findAllPaged(principal.getName(), title, request.toPageable()));
        }

        @Operation(
                summary = "Lấy danh sách danh mục quiz (Public - Không cần JWT)",
                description = """
                        **Lấy danh sách tất cả các danh mục quiz đang hoạt động (KHÔNG cần xác thực).**
                        
                        API này trả về thông tin giống hệt `/quiz/category` nhưng không yêu cầu JWT token.
                        
                        **Tính năng tìm kiếm:**
                        - `title`: Tìm kiếm theo tên hoặc mã danh mục (tìm kiếm không phân biệt hoa thường)
                        - Có thể tìm theo title hoặc code của danh mục
                        - Để trống để lấy tất cả danh mục
                        
                        **Hỗ trợ phân trang tùy chọn:**
                        - Nếu không truyền tham số `page` và `size`: trả về tất cả danh mục dưới dạng List<CategoryResponse>
                        - Nếu truyền tham số `page` hoặc `size`: trả về dữ liệu phân trang dưới dạng PageResponse<CategoryResponse>
                        
                        **Tham số phân trang:**
                        - `page`: Số trang (bắt đầu từ 0), mặc định = 0
                        - `size`: Số lượng item mỗi trang, mặc định = 10
                        - `sort`: Sắp xếp (ví dụ: "order,asc" hoặc "title,desc")
                        
                        **Response types:**
                        - Không phân trang: `List<CategoryResponse>`
                        - Có phân trang: `PageResponse<CategoryResponse>`
                        
                        **Use cases:**
                        - Landing page hiển thị danh sách danh mục
                        - Tìm kiếm danh mục cho guest users
                        - Preview danh mục trước khi đăng ký/đăng nhập
                        
                        **Lưu ý:**
                        - `purchasedInfo` sẽ là `null` (vì không có thông tin user)
                        - Phù hợp cho landing page, preview, hoặc guest users
                        - Không conflict với endpoint `/quiz/category` (có JWT)
                        """
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200",
                        description = "Lấy danh sách danh mục thành công. Response type phụ thuộc vào việc có sử dụng phân trang hay không: List<CategoryResponse> (không phân trang) hoặc PageResponse<CategoryResponse> (có phân trang)"),
                @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
                @ApiResponse(responseCode = "default",
                        description = "Lỗi không mong đợi",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @GetMapping("/quiz/category/public")
        public Object getAllCategoryPublic(
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Tìm kiếm theo tên danh mục hoặc mã danh mục - tùy chọn",
                    example = "TOEIC"
                )
                @RequestParam(required = false) String title,
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số trang (bắt đầu từ 0) - tùy chọn. Nếu có thì sẽ trả về phân trang",
                    example = "0",
                    required = false
                )
                @RequestParam(required = false) Integer page,
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Số lượng item mỗi trang - tùy chọn. Nếu có thì sẽ trả về phân trang",
                    example = "10",
                    required = false
                )
                @RequestParam(required = false) Integer size,
                @io.swagger.v3.oas.annotations.Parameter(
                    description = "Sắp xếp theo trường (format: 'field,direction') - tùy chọn",
                    example = "order,asc",
                    required = false
                )
                @RequestParam(required = false) String sort) {

                // Nếu không có tham số phân trang, trả về danh sách không phân trang
                if (page == null && size == null) {
                        log.info("getAllCategoryPublic: public access (no authentication, no pagination)");
                        return quizCategoryService.findAllPublic(title);
                }

                // Nếu có tham số phân trang, sử dụng phân trang
                log.info("getAllCategoryPublic: public access with pagination - page: {}, size: {}, sort: {}",
                        page, size, sort);

                PaginationRequest request = PaginationRequest.of(page, size, sort);

                return PageResponse.of(quizCategoryService.findAllPublicPaged(title, request.toPageable()));
        }


        @Operation(
                summary = "Lấy chi tiết quiz theo ID (Hỗ trợ Khôi phục Bài làm dở)",
                description = """
                        **Lấy thông tin chi tiết của một quiz cụ thể, bao gồm tất cả câu hỏi.**

                        API này trả về toàn bộ thông tin của một quiz để user có thể bắt đầu làm bài.
                        
                        **Khôi phục Bài làm dở (Resume):**
                        - API này sẽ tự động tìm kiếm bản lưu tạm (Draft) gần nhất của người dùng cho quiz này.
                        - Nếu tìm thấy, phản hồi sẽ bao gồm `savedAnswers` (danh sách câu trả lời đã lưu) và `activityStartTime` (thời gian bắt đầu của bản nháp đó).
                        - Frontend sử dụng dữ liệu này để khôi phục lại trạng thái bài làm.

                        **Response bao gồm:**
                        - Thông tin quiz (tên, thời gian, ...)
                        - Tất cả câu hỏi và các lựa chọn đáp án.
                        - (MỚI) `savedAnswers`: Map các câu trả lời đã lưu (nếu có).
                        - (MỚI) `activityStartTime`: Thời gian bắt đầu bản nháp (nếu có).

                        **Yêu cầu:**
                        - Phải có JWT token hợp lệ
                        - User phải có quyền truy cập quiz (đã mua hoặc miễn phí)
                        """
        )
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy chi tiết quiz thành công (có thể bao gồm bài làm dở)."),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập - Chưa mua quiz này", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy quiz với ID này", content = @Content),
                        @ApiResponse(responseCode = "default",
                                description = "Lỗi không mong đợi",
                                content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @GetMapping("/quiz/{quizId}")
        public QuizInfoResponse getQuizInfo(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
                        @io.swagger.v3.oas.annotations.Parameter(
                                description = "ID của quiz cần lấy thông tin",
                                required = true,
                                example = "123"
                        )
                        @PathVariable("quizId") Long quizId) throws Exception {
                log.info("getQuizInfo: user {}, quizId {}", principal.getName(), quizId);
                return quizMasterService.getQuizInfo(principal.getName(), quizId);
        }

        @Operation(
                summary = "Nộp bài quiz hoặc Lưu tạm (Draft Save)",
                description = """
                        **Nộp câu trả lời của user và nhận kết quả chấm điểm HOẶC lưu tạm tiến trình.**
                        
                        API này xử lý cả hai kịch bản:

                        **1. Nộp bài cuối cùng (Mặc định):**
                        - Gửi request mà không có cờ `isDraft` (hoặc `isDraft: false`).
                        - Backend sẽ chấm điểm, lưu kết quả, và trả về điểm số chi tiết.
                        
                        **2. Lưu tạm (Draft Save):**
                        - Gửi request với cờ: **`"isDraft": true`**.
                        - Backend chỉ lưu các câu trả lời, **không chấm điểm**, và đánh dấu bài làm là "chưa hoàn thành".
                        - Response trả về sẽ không chứa điểm số.

                        **Request body:**
                        - `data`: Danh sách câu trả lời của user.
                        - `startTime`, `endTime` (Chỉ cần thiết khi nộp bài cuối cùng).
                        - `isDraft` (Boolean): Cờ xác định hành động.

                        **Response (Nộp bài):**
                        - Điểm số tổng (totalScore, percentage), trạng thái (pass), v.v.

                        **Response (Lưu tạm):**
                        - Một đối tượng rỗng hoặc thông báo "Draft saved successfully."
                        """
        )
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Nộp bài (có điểm) hoặc Lưu tạm (không có điểm) thành công."),
                        @ApiResponse(responseCode = "400", description = "Định dạng câu trả lời không hợp lệ", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền làm quiz này", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy quiz với ID này", content = @Content),
                        @ApiResponse(responseCode = "default",
                                description = "Lỗi không mong đợi",
                                content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @PostMapping("/quiz/{quizId}")
        public SubmitAnswerResponse submitAnswerData(
                    @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
                    @io.swagger.v3.oas.annotations.Parameter(
                            description = "ID của quiz đang nộp bài",
                            required = true,
                            example = "123"
                    )
                    @PathVariable("quizId") Long quizId,
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Danh sách câu trả lời của user",
                            required = true
                    )
                    @RequestBody SubmitAnswerRequest request) throws Exception {
                log.info("submitAnswerData: user {}, quizId {}", principal.getName(), quizId);
                return quizMasterService.submitAnswer(principal.getName(), quizId, request);
        }

        @Operation(
                summary = "Giải thích câu trả lời khi đang làm bài dở",
                description = """
                        **API để giải thích câu trả lời khi user đang làm bài quiz chưa hoàn thành.**

                        API này cho phép user kiểm tra đáp án và nhận giải thích cho một câu hỏi cụ thể trong khi đang làm bài.
                        
                        **Request body bao gồm:**
                        - `quizId`: ID của bài quiz đang làm
                        - `questionId`: ID của câu hỏi cần giải thích
                        - `answerData`: Mảng boolean thể hiện đáp án user đã chọn (vd: [false, true, false, false])
                        
                        **Response bao gồm:**
                        - `isCorrect`: Có đúng hay không (boolean)
                        - `correctAnswer`: Mảng đáp án đúng (vd: [true, false, false, false])
                        - `correctAnswerDetails`: Chi tiết các đáp án đúng với index và nội dung text
                        - `explanation`: Lời giải thích từ correct_msg (nếu đúng) hoặc incorrect_msg (nếu sai)
                        - `score`: Điểm đạt được cho câu này (points nếu đúng, 0 nếu sai)

                        **Use cases:**
                        - User muốn kiểm tra đáp án ngay khi làm xong một câu
                        - Học tập và hiểu rõ lý do đúng/sai của từng câu
                        - Cải thiện kết quả trước khi nộp bài cuối cùng

                        **Yêu cầu:**
                        - Phải có JWT token hợp lệ
                        - User phải có quyền truy cập quiz này
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Giải thích câu trả lời thành công"),
                        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập quiz này", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy quiz hoặc câu hỏi", content = @Content),
                        @ApiResponse(responseCode = "default",
                                description = "Lỗi không mong đợi",
                                content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @SecurityRequirement(name = "bearerAuth")
        @PostMapping("/quiz/explain")
        public ExplainAnswerResponse explainAnswer(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "Thông tin câu hỏi và đáp án cần giải thích",
                                required = true
                        )
                        @Valid @RequestBody ExplainAnswerRequest request) throws AppException {
                log.info("explainAnswer: user {}, quizId {}, questionId {}",
                        principal.getName(), request.getQuizId(), request.getQuestionId());
                return questionService.explainAnswer(principal.getName(), request);
        }

        @Operation(
                summary = "Lấy lịch sử làm bài của người dùng",
                description = """
                        **Lấy lịch sử tất cả các lần làm bài quiz của người dùng với filter nâng cao.**

                        API này trả về danh sách tất cả các lần làm bài đã hoàn thành (không bao gồm draft).
                        
                        **Tính năng:**
                        - Lấy tất cả lịch sử làm bài (nếu không truyền quizId)
                        - Lọc theo quiz cụ thể (quizId)
                        - Lọc theo category (categoryCode)
                        - Lọc theo khoảng thời gian (fromDate, toDate)
                        - Sắp xếp theo: time (thời gian), score (điểm số), percentage (phần trăm)
                        - Hỗ trợ phân trang

                        **Response bao gồm:**
                        - Thông tin quiz (tên, slug, category, loại)
                        - Thời gian làm bài (bắt đầu, kết thúc, thời lượng)
                        - Kết quả (điểm, số câu đúng/sai, phần trăm, pass/fail)
                        - Metadata phân trang (page, size, totalElements, totalPages)

                        **Use cases:**
                        - Xem tất cả lịch sử làm bài của user
                        - Xem lịch sử làm một quiz cụ thể (để so sánh các lần làm)
                        - Phân tích tiến độ học tập theo category
                        - Xem kết quả trong khoảng thời gian cụ thể

                        **Yêu cầu:**
                        - Phải có JWT token hợp lệ
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy lịch sử làm bài thành công"),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy quiz với ID này (nếu filter theo quizId)", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content),
                        @ApiResponse(responseCode = "default",
                                description = "Lỗi không mong đợi",
                                content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping("/quiz/history")
        public com.hth.udecareer.model.response.QuizHistoryWrapperResponse getQuizHistory(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
                        @Valid
                        @ParameterObject
                        com.hth.udecareer.model.dto.QuizHistoryRequestDto request) throws AppException {
                log.info("getQuizHistory: user {}, request: {}", principal.getName(), request);
                
                return quizMasterService.getQuizHistoryWithStats(
                                        principal.getName(),
                                        request.getQuizId(),
                                        request.getCategoryCode(),
                                        request.getFromDate(),
                                        request.getToDate(),
                                        request.getSortBy(),
                                        request.getSortDirection(),
                                        request.getPage(),
                                        request.getSize());
        }

        @Operation(
                summary = "Xem chi tiết kết quả làm bài",
                description = """
                        **Xem chi tiết từng câu hỏi trong một lần làm bài cụ thể.**

                        API này trả về:
                        - Tóm tắt kết quả (tổng câu, số câu đúng/sai, điểm, pass/fail)
                        - Chi tiết từng câu hỏi:
                          * Nội dung câu hỏi
                          * Các đáp án (với đánh dấu đáp án đúng và đáp án user chọn)
                          * User đã chọn đáp án nào
                          * Đáp án đúng là gì
                          * Giải thích tại sao đúng/sai
                          * Điểm đạt được cho câu này

                        **Use cases:**
                        - Ôn tập sau khi làm bài
                        - Hiểu rõ lỗi sai
                        - Học từ giải thích
                        - So sánh các lần làm

                        **Yêu cầu:**
                        - Phải có JWT token hợp lệ
                        - User phải là người làm bài đó (kiểm tra ownership)
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy chi tiết kết quả thành công"),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực - Token không hợp lệ hoặc thiếu", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền xem kết quả này", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy activity", content = @Content),
                        @ApiResponse(responseCode = "default",
                                description = "Lỗi không mong đợi",
                                content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @SecurityRequirement(name = "bearerAuth")
        @GetMapping("/quiz/history/{activityId}/detail")
        public com.hth.udecareer.model.response.QuizResultDetailResponse getQuizResultDetail(
                        @io.swagger.v3.oas.annotations.Parameter(hidden = true) Principal principal,
                        @io.swagger.v3.oas.annotations.Parameter(
                                description = "ID của activity (lần làm bài) - lấy từ API /quiz/history",
                                required = true,
                                example = "12345"
                        )
                        @PathVariable("activityId") Long activityId) throws AppException {
                log.info("getQuizResultDetail: user {}, activityId {}", principal.getName(), activityId);
                return quizMasterService.getQuizResultDetail(principal.getName(), activityId);
        }
}
