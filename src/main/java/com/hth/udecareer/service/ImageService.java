package com.hth.udecareer.service;

import com.hth.udecareer.model.response.ImageR2Response;
import com.hth.udecareer.model.response.ImageResponse;
import com.hth.udecareer.model.response.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
//    ImageResponse uploadImage(MultipartFile file);
//    String deleteImage(String publicId);
//    ImageR2Response uploadR2Image(MultipartFile file);

    String uploadImageOnServer(MultipartFile file) throws IOException;
    
    MediaUploadResponse uploadImageWithMediaKey(MultipartFile file, Long userId) throws IOException;
}
