package com.hth.udecareer.service.Impl;

import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.entities.MediaArchiveEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.response.ImageR2Response;
import com.hth.udecareer.model.response.ImageResponse;
import com.hth.udecareer.model.response.MediaUploadResponse;
import com.hth.udecareer.repository.MediaArchiveRepository;
import com.hth.udecareer.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final MediaArchiveRepository mediaArchiveRepository;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

//    @Value("${app.domain}")
//    private String appDomain;

    @Value("${app.asset-domain}")
    private String assetDomain;


    @Override
    public String uploadImageOnServer(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Lấy root project (tránh Tomcat temp)
        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, uploadDir);

        // Tạo thư mục nếu chưa có
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file duy nhất
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + extension;

        // Đường dẫn file cuối cùng
        Path filePath = uploadPath.resolve(newFileName);

        // Lưu file
        file.transferTo(filePath.toFile());

        // Trả về URL truy cập qua Asset domain
        return assetDomain + "/" + uploadDir + "/" + newFileName;
    }

    @Override
    public MediaUploadResponse uploadImageWithMediaKey(MultipartFile file, Long userId) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Lấy root project
        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, uploadDir);
        
        // Tạo thư mục nếu chưa có
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo mediaKey (UUID không có extension)
        String mediaKey = UUID.randomUUID().toString();
        
        // Tạo tên file với extension
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = mediaKey + extension;

        // Đường dẫn file cuối cùng
        Path filePath = uploadPath.resolve(newFileName);

        // Lưu file
        file.transferTo(filePath.toFile());

        // Get file URL (relative path for database)
        String mediaUrl = "/" + uploadDir + "/" + newFileName;
        
        // Get full URL for response (with Asset domain for public access)
        String fullMediaUrl = assetDomain + "/" + uploadDir + "/" + newFileName;

        // Determine media type
        String mediaType = file.getContentType();
        if (mediaType == null) {
            mediaType = "image/jpeg"; // Default
        }

        // Get image dimensions if it's an image
        Integer width = null;
        Integer height = null;
        if (mediaType != null && mediaType.startsWith("image/")) {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image != null) {
                    width = image.getWidth();
                    height = image.getHeight();
                }
            } catch (IOException e) {
                log.warn("Failed to read image dimensions: {}", e.getMessage());
            }
        }

        // Build settings JSON
        Map<String, Object> settingsMap = new HashMap<>();
        if (width != null) {
            settingsMap.put("width", width);
        }
        if (height != null) {
            settingsMap.put("height", height);
        }
        String settingsJson = null;
        if (!settingsMap.isEmpty()) {
            try {
                settingsJson = objectMapper.writeValueAsString(settingsMap);
            } catch (Exception e) {
                log.warn("Failed to convert settings to JSON: {}", e.getMessage());
            }
        }

        // Save to media archive
        MediaArchiveEntity media = MediaArchiveEntity.builder()
                .objectSource("feed")
                .mediaKey(mediaKey)
                .userId(userId)
                .mediaType(mediaType)
                .driver("local")
                .mediaPath(filePath.toString())
                .mediaUrl(mediaUrl)
                .isActive(1)
                .settings(settingsJson)
                .build();

        media = mediaArchiveRepository.save(media);

        return MediaUploadResponse.builder()
                .media(MediaUploadResponse.MediaInfo.builder()
                        .url(fullMediaUrl)
                        .mediaKey(mediaKey)
                        .type(mediaType)
                        .width(width)
                        .height(height)
                        .build())
                .build();
    }
}
