package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.response.ImageR2Response;
import com.hth.udecareer.model.response.ImageResponse;
import com.hth.udecareer.model.response.MediaUploadResponse;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Slf4j
@RestController
@ApiPrefixController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final UserRepository userRepository;

//    // cloudinary
//    @PostMapping("/images")
//    public ImageResponse uploadImage(@RequestParam("file") MultipartFile file) {
//        return imageService.uploadImage(file);
//    }
//
//    // cloudinary
//    @DeleteMapping("/images")
//    public ApiResponse deleteImage(@RequestParam("publicId") String publicId) {
//        String text = imageService.deleteImage(publicId);
//        return ApiResponse.success(text);
//    }

//    // Cloudflare R2
//    @PostMapping("/r2/images")
//    public ImageR2Response uploadR2Image(@RequestParam("file") MultipartFile file) {
//        return imageService.uploadR2Image(file);
//    }

    // on server - Simple upload (backward compatible)
    @PostMapping("/image")
    public ApiResponse uploadImageOnServer(@RequestParam("file") MultipartFile file) throws IOException {
        String text = imageService.uploadImageOnServer(file);
        return ApiResponse.success(text);
    }

    @Operation(
            summary = "Upload ảnh với mediaKey (cho feeds)",
            description = """
                    **Upload file ảnh và lưu vào MediaArchive để sử dụng trong bài viết**
                    
                    **Request Parameters:**
                    - `file`: File ảnh cần upload (multipart/form-data)
                    
                    **Response:** Thông tin media đã upload (url, media_key, type, width, height)
                    
                    **Lưu ý:** Endpoint này yêu cầu authentication và sẽ lưu media vào MediaArchive với mediaKey
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Upload thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MediaUploadResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "File không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/image/upload")
    public ResponseEntity<MediaUploadResponse> uploadImageWithMediaKey(
            @Parameter(hidden = true) Principal principal,
            @RequestParam("file") MultipartFile file) throws IOException, AppException {
        
        // Get userId from principal
        Long userId = null;
        if (principal != null) {
            String email = principal.getName();
            userId = userRepository.findByEmail(email)
                    .map(user -> user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        }
        
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        MediaUploadResponse response = imageService.uploadImageWithMediaKey(file, userId);
        return ResponseEntity.ok(response);
    }
}
