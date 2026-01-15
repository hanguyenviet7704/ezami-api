// src/main/java/com/hth/udecareer/config/SwaggerConfig.java
package com.hth.udecareer.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Ezami API - Tài liệu API")
                                                .version("v1.0.0")
                                                .description("""
                                                                # Tài liệu API chính thức cho ứng dụng Ezami

                                                                Tài liệu này mô tả tất cả các API endpoints có sẵn cho ứng dụng Ezami, bao gồm:
                                                                - **Authentication & User Management**: Đăng ký, đăng nhập, quản lý tài khoản
                                                                - **Quiz Management**: Quản lý quiz, câu hỏi, nộp bài kiểm tra
                                                                - **Post (Article) Management**: Quản lý bài viết, danh mục

                                                                ## Cách sử dụng API

                                                                ### 1. API Công khai (Public)
                                                                Các API không có biểu tượng ổ khóa có thể truy cập trực tiếp mà không cần xác thực:
                                                                - `/authenticate` - Đăng nhập
                                                                - `/register` - Đăng ký tài khoản
                                                                - `/verification-code` - Tạo mã xác thực
                                                                - `/post/*` - Xem bài viết công khai

                                                                ### 2. API Được bảo vệ (Protected)
                                                                Các API có biểu tượng ổ khóa yêu cầu JWT Token:

                                                                **Các bước thực hiện:**
                                                                1. Gọi API `/authenticate` với email và password để nhận JWT token
                                                                2. Copy token từ response
                                                                3. Bấm nút **"Authorize"** (màu xanh ở góc trên)
                                                                4. Dán token vào ô "Value" (KHÔNG thêm "Bearer " vào trước)
                                                                5. Bấm "Authorize" để lưu
                                                                6. Giờ bạn có thể gọi các API được bảo vệ

                                                                ### 3. Mã lỗi (Error Codes)
                                                                API sử dụng HTTP status codes chuẩn:
                                                                - `200` - Thành công
                                                                - `400` - Dữ liệu request không hợp lệ
                                                                - `401` - Chưa xác thực hoặc token không hợp lệ
                                                                - `403` - Không có quyền truy cập
                                                                - `404` - Không tìm thấy tài nguyên
                                                                - `500` - Lỗi server

                                                                ### 4. Request Format
                                                                - Content-Type: `application/json`
                                                                - Tất cả request body phải là JSON hợp lệ
                                                                - Tất cả date/time theo định dạng ISO-8601

                                                                ### 5. Google OAuth
                                                                Hỗ trợ đăng nhập bằng Google:
                                                                - **Web**: Sử dụng `/auth/google/login` để lấy URL, sau đó xử lý callback
                                                                - **Mobile**: Sử dụng `/api/auth/google/redirect` với authorization code
                                                                """)
                                                .contact(new Contact()
                                                                .name("Ezami Support")
                                                                .email("support@ezami.io"))
                                                .license(new License()
                                                                .name("Proprietary")
                                                                .url("https://ezami.io/terms")))
                                .servers(List.of(
                                                new Server()
                                                                .url("")
                                                                .description("API Server")))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .in(SecurityScheme.In.HEADER)
                                                                                .name("Authorization")
                                                                                .description("""
                                                                                                **JWT Authorization header**

                                                                                                Để sử dụng:
                                                                                                1. Gọi API `/authenticate` để lấy JWT token
                                                                                                2. Bấm nút "Authorize" ở trên
                                                                                                3. Dán token vào ô "Value" (chỉ token, KHÔNG bao gồm "Bearer ")
                                                                                                4. Bấm "Authorize" để lưu

                                                                                                Token sẽ tự động được thêm vào header của các request tiếp theo.
                                                                                                """)));
        }
}
