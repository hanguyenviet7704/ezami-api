package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.LessonProgressRequest;
import com.hth.udecareer.model.response.CourseProgressResponse;
import com.hth.udecareer.model.response.LessonProgressResponse;
import com.hth.udecareer.service.LessonProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    @Operation(summary = "Cập nhật tiến độ xem bài học")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật tiến độ thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressResponse> updateProgress(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonProgressRequest request,
            Principal principal) {

        String email = principal.getName();

        LessonProgressResponse response = lessonProgressService.updateLessonProgress(email, request, lessonId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy tiến độ lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy tiến độ thành công"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressResponse> getLessonProgress(
            @PathVariable Long lessonId,
            Principal principal) {

        LessonProgressResponse response = lessonProgressService.getLessonProgress(principal.getName(), lessonId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Lấy tiến độ course")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy tiến độ thành công"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long courseId,
            Principal principal) {

        CourseProgressResponse response = lessonProgressService.getCourseProgress(principal.getName(), courseId);
        return ResponseEntity.ok(response);

    }
}
