package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.model.request.TranslationRequest;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.response.TranslationResponse;
import com.hth.udecareer.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "Translation API", description = "API quản lý nội dung đa ngôn ngữ")
public class TranslationController {

    private final TranslationService translationService;

    // ============= PUBLIC ENDPOINTS =============

    @GetMapping("/translations/{entityType}/{entityId}")
    @Operation(summary = "Lấy translations của entity",
            description = "Lấy tất cả translations cho một entity theo ngôn ngữ hiện tại (từ Accept-Language header)")
    public ResponseEntity<ApiResponse> getTranslations(
            @Parameter(description = "Loại entity (badge, space, topic, question)")
            @PathVariable String entityType,
            @Parameter(description = "ID của entity")
            @PathVariable Long entityId) {

        Map<String, String> translations = translationService.getTranslations(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(translations));
    }

    @GetMapping("/translations/{entityType}/{entityId}/all")
    @Operation(summary = "Lấy tất cả translations của entity (tất cả ngôn ngữ)",
            description = "Lấy translations cho một entity theo tất cả các ngôn ngữ có sẵn")
    public ResponseEntity<ApiResponse> getAllTranslations(
            @Parameter(description = "Loại entity (badge, space, topic, question)")
            @PathVariable String entityType,
            @Parameter(description = "ID của entity")
            @PathVariable Long entityId) {

        Map<String, Map<String, String>> translations = translationService.getAllTranslations(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(translations));
    }

    @GetMapping("/translations/{entityType}/{entityId}/languages")
    @Operation(summary = "Lấy danh sách ngôn ngữ có sẵn",
            description = "Lấy danh sách các ngôn ngữ có bản dịch cho entity")
    public ResponseEntity<ApiResponse> getAvailableLanguages(
            @Parameter(description = "Loại entity")
            @PathVariable String entityType,
            @Parameter(description = "ID của entity")
            @PathVariable Long entityId) {

        List<String> languages = translationService.getAvailableLanguages(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(languages));
    }

    // ============= ADMIN ENDPOINTS =============

    @PostMapping("/admin/translations")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "[Admin] Tạo hoặc cập nhật translation",
            description = "Tạo mới hoặc cập nhật một translation. Yêu cầu quyền Admin hoặc Moderator")
    public ResponseEntity<ApiResponse> saveTranslation(
            @Valid @RequestBody TranslationRequest request) {

        TranslationResponse response = translationService.saveTranslation(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/admin/translations/{entityType}/{entityId}/batch")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "[Admin] Batch save translations",
            description = "Lưu nhiều translations cùng lúc cho một entity")
    public ResponseEntity<ApiResponse> batchSaveTranslations(
            @Parameter(description = "Loại entity")
            @PathVariable String entityType,
            @Parameter(description = "ID của entity")
            @PathVariable Long entityId,
            @Parameter(description = "Map<language, Map<fieldName, value>>")
            @RequestBody Map<String, Map<String, String>> translations) {

        List<TranslationResponse> responses = translationService.saveTranslations(entityType, entityId, translations);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/admin/translations/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "[Admin] Lấy tất cả translations cho admin",
            description = "Lấy danh sách chi tiết tất cả translations của một entity")
    public ResponseEntity<ApiResponse> getTranslationsForAdmin(
            @Parameter(description = "Loại entity")
            @PathVariable String entityType,
            @Parameter(description = "ID của entity")
            @PathVariable Long entityId) {

        List<TranslationResponse> translations = translationService.getTranslationsForAdmin(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(translations));
    }

    @GetMapping("/admin/translations/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "[Admin] Lấy translation theo ID",
            description = "Lấy chi tiết một translation theo ID")
    public ResponseEntity<ApiResponse> getTranslationById(
            @Parameter(description = "ID của translation")
            @PathVariable Long id) {

        TranslationResponse response = translationService.getTranslationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/admin/translations/{entityType}/{entityId}/{fieldName}/{language}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "[Admin] Xóa một translation",
            description = "Xóa một translation cụ thể")
    public ResponseEntity<ApiResponse> deleteTranslation(
            @Parameter(description = "Loại entity")
            @PathVariable String entityType,
            @Parameter(description = "ID của entity")
            @PathVariable Long entityId,
            @Parameter(description = "Tên field")
            @PathVariable String fieldName,
            @Parameter(description = "Mã ngôn ngữ")
            @PathVariable String language) {

        translationService.deleteTranslation(entityType, entityId, fieldName, language);
        return ResponseEntity.ok(ApiResponse.success("Translation deleted successfully"));
    }

    @DeleteMapping("/admin/translations/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "[Admin] Xóa tất cả translations của entity",
            description = "Xóa tất cả translations cho một entity")
    public ResponseEntity<ApiResponse> deleteAllTranslations(
            @Parameter(description = "Loại entity")
            @PathVariable String entityType,
            @Parameter(description = "ID của entity")
            @PathVariable Long entityId) {

        translationService.deleteAllTranslations(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("All translations deleted successfully"));
    }

    @GetMapping("/admin/translations/stats/{entityType}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODERATOR')")
    @Operation(summary = "[Admin] Thống kê translations",
            description = "Lấy thống kê số lượng translations theo ngôn ngữ")
    public ResponseEntity<ApiResponse> getTranslationStats(
            @Parameter(description = "Loại entity")
            @PathVariable String entityType) {

        Map<String, Long> stats = translationService.getTranslationStats(entityType);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
