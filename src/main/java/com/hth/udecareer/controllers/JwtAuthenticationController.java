package com.hth.udecareer.controllers;

import javax.validation.Valid;

import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.model.request.CheckVerificationCodeRequest;
import com.hth.udecareer.service.VerificationCodeService;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.service.Impl.GoogleAuthService;
import com.hth.udecareer.utils.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.RegisterRequest;
import com.hth.udecareer.model.request.RegisterWithCodeRequest;
import com.hth.udecareer.model.request.VerificationRequest;
import com.hth.udecareer.model.response.UserResponse;
import com.hth.udecareer.security.JwtRequest;
import com.hth.udecareer.security.JwtResponse;
import com.hth.udecareer.security.JwtTokenUtil;
import com.hth.udecareer.security.JwtUserDetailsService;
import com.hth.udecareer.service.UserService;
import com.hth.udecareer.config.GoogleOAuthConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication & User Management")
public class JwtAuthenticationController {

        private final AuthenticationManager authenticationManager;
        private final JwtTokenUtil jwtTokenUtil;
        private final JwtUserDetailsService userDetailsService;
        private final UserService userService;

        private final VerificationCodeService verificationCodeService;

        private final GoogleAuthService googleAuthService;
        private final GoogleOAuthConfig googleOAuthConfig;
        
        private final CookieUtil cookieUtil;
        private final com.hth.udecareer.repository.AffiliateLinkRepository affiliateLinkRepository;

        @Operation(
                summary = "Authenticate user (Login)",
                description = """
                        Generates a JWT token for a valid user and saves it to cookie.

                        **Available Paths:**
                        - POST /api/auth/authenticate (Primary - Recommended)
                        - POST /authenticate (Alias - Backward compatible)

                        Both paths work identically.
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
                        @ApiResponse(responseCode = "403", description = "User is disabled", content = @Content),
                        @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @PostMapping({"/authenticate", "/api/auth/authenticate"})
        public JwtResponse createAuthenticationToken(
                        @Valid @RequestBody JwtRequest authenticationRequest,
                        HttpServletResponse response) throws Exception {
                log.info("createAuthenticationToken: user {}", authenticationRequest.getUsername());
                authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

                final UserDetails userDetails = userDetailsService
                                .loadUserByUsername(authenticationRequest.getUsername());

                final String token = jwtTokenUtil.generateToken(userDetails);
                
                log.info("JWT token generated for user: {}", authenticationRequest.getUsername());

                // Lưu token vào cookie - PHẢI được gọi TRƯỚC khi return
                // Kiểm tra response chưa bị committed
                if (response.isCommitted()) {
                    log.error("Response already committed before setting cookie for user: {}", authenticationRequest.getUsername());
                } else {
                    try {
                        cookieUtil.addJwtCookie(response, token);
                        log.info("JWT cookie set successfully for user: {}", authenticationRequest.getUsername());
                        
                        // Verify cookie header đã được set
                        String setCookieHeader = response.getHeader("Set-Cookie");
                        if (setCookieHeader != null) {
                            // Extract token from Set-Cookie header for logging
                            String tokenInCookie = extractTokenFromSetCookie(setCookieHeader);
                            if (tokenInCookie != null && tokenInCookie.equals(token)) {
                                log.info("Set-Cookie header verified: token matches (length: {})", token.length());
                            } else {
                                log.warn("Set-Cookie header token mismatch! Expected length: {}, Got length: {}", 
                                    token.length(), tokenInCookie != null ? tokenInCookie.length() : 0);
                            }
                        } else {
                            log.warn("Set-Cookie header not found in response after setting cookie!");
                        }
                    } catch (Exception e) {
                        log.error("Failed to set JWT cookie for user: {}, error: {}", 
                            authenticationRequest.getUsername(), e.getMessage(), e);
                        // Vẫn trả về token trong response body để client có thể lưu
                    }
                }

                // Trả về token trong response body (fallback nếu cookie không work)
                // Return JwtResponse trực tiếp để GlobalControllerAdvice không wrap
                return new JwtResponse(token);
        }

        @Operation(
                summary = "Sign up a new user (Legacy)", 
                description = """
                        Đăng ký tài khoản mới (Legacy - không cần mã xác thực).
                        
                        **Lưu ý:** API này không yêu cầu mã xác thực email. Khuyến nghị sử dụng `/register` với mã xác thực.
                        
                        **Affiliate ID:** Nếu người dùng đăng ký thông qua affiliate link, truyền `affiliateId` để track referral.
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or user already exists"),
                        @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @PostMapping("/signup")
        public UserResponse signup(
                        @RequestBody @Valid RegisterRequest request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse response) throws Exception {
                // Lấy affiliateId từ cookie affiliate_ref nếu request chưa có
                if (request.getAffiliateId() == null) {
                    Long affiliateIdFromCookie = getAffiliateIdFromCookie(httpRequest);
                    if (affiliateIdFromCookie != null) {
                        request.setAffiliateId(affiliateIdFromCookie);
                        log.info("signup: affiliateId from cookie: {}", affiliateIdFromCookie);
                    }
                }
                
                log.info("signup: user {}, appCode {}, affiliateId {}", request.getEmail(), request.getAppCode(), request.getAffiliateId());
                UserResponse userResponse = userService.signup(request);
                
                // Tạo JWT token và lưu vào cookie
                final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
                final String token = jwtTokenUtil.generateToken(userDetails);
                
                log.info("JWT token generated for signup user: {}", request.getEmail());
                
                // Lưu token vào cookie - PHẢI được gọi TRƯỚC khi return
                if (response.isCommitted()) {
                    log.error("Response already committed before setting cookie for signup");
                } else {
                    try {
                        cookieUtil.addJwtCookie(response, token);
                        log.info("JWT cookie set successfully for signup user: {}", request.getEmail());
                    } catch (Exception e) {
                        log.error("Failed to set JWT cookie for signup user: {}, error: {}", 
                            request.getEmail(), e.getMessage(), e);
                    }
                }
                
                return userResponse;
        }

        @Operation(
                summary = "Sign up a new user with verification code", 
                description = """
                        Đăng ký tài khoản mới với mã xác thực email.
                        
                        **Flow:**
                        1. Gọi `/verification-code` để nhận mã xác thực qua email
                        2. Gọi API này với mã xác thực để hoàn tất đăng ký
                        
                        **Affiliate ID:** 
                        - Nếu người dùng đăng ký thông qua affiliate link, truyền `affiliateId` để track referral
                        - Affiliate ID sẽ được lưu vào user metadata để tracking và tính commission sau này
                        - Nếu không có affiliate, có thể bỏ qua field này
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or invalid code"),
                        @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @PostMapping("/register")
        public UserResponse signup(
                        @Valid @RequestBody RegisterWithCodeRequest request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse response) throws Exception {
                // Lấy affiliateId từ cookie affiliate_ref nếu request chưa có
                if (request.getAffiliateId() == null) {
                    Long affiliateIdFromCookie = getAffiliateIdFromCookie(httpRequest);
                    if (affiliateIdFromCookie != null) {
                        request.setAffiliateId(affiliateIdFromCookie);
                        log.info("register: affiliateId from cookie: {}", affiliateIdFromCookie);
                    }
                }
                
                log.info("register: user {}, appCode {}, affiliateId {}", request.getEmail(), request.getAppCode(), request.getAffiliateId());
                UserResponse userResponse = userService.signup(request);
                
                // Tạo JWT token và lưu vào cookie
                final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
                final String token = jwtTokenUtil.generateToken(userDetails);
                
                log.info("JWT token generated for register user: {}", request.getEmail());
                
                // Lưu token vào cookie - PHẢI được gọi TRƯỚC khi return
                if (response.isCommitted()) {
                    log.error("Response already committed before setting cookie for register");
                } else {
                    try {
                        cookieUtil.addJwtCookie(response, token);
                        log.info("JWT cookie set successfully for register user: {}", request.getEmail());
                    } catch (Exception e) {
                        log.error("Failed to set JWT cookie for register user: {}, error: {}", 
                            request.getEmail(), e.getMessage(), e);
                    }
                }
                
                return userResponse;
        }

        @Operation(summary = "Generate verification code", description = "Sends a verification code to the user's email for registration or password reset.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Code sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid email or type"),
                        @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @PostMapping("/verification-code")
        public void generateVerificationCode(@Valid @RequestBody VerificationRequest request) throws Exception {
                log.info("generateVerificationCode: user {}, type {}", request.getEmail(), request.getType());
                userService.generateVerificationCode(request.getEmail(), request.getType());
        }

        @Operation(summary = "Check verification code", description = "Validates the provided verification code.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Code is valid"),
                        @ApiResponse(responseCode = "400", description = "Invalid, incorrect, or expired code"),
                        @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @PostMapping("/verification-code/check")
        public void checkVerificationCode(@Valid @RequestBody CheckVerificationCodeRequest request)
                        throws AppException {
                log.info("checkVerificationCode: email {}, type {}", request.getEmail(), request.getType());
                verificationCodeService.checkVerificationCode(request.getEmail(), request.getVerificationCode(),
                                request.getType());
        }

        /**
         * Extract token value from Set-Cookie header string
         * Format: "accessToken=<token>; Path=/; Max-Age=..."
         */
        private String extractTokenFromSetCookie(String setCookieHeader) {
            if (setCookieHeader == null || setCookieHeader.isEmpty()) {
                return null;
            }
            // Find the token value between cookie name and first semicolon
            String cookieName = "accessToken=";
            int startIndex = setCookieHeader.indexOf(cookieName);
            if (startIndex == -1) {
                return null;
            }
            startIndex += cookieName.length();
            int endIndex = setCookieHeader.indexOf(';', startIndex);
            if (endIndex == -1) {
                // No semicolon found, token goes to end of string
                endIndex = setCookieHeader.length();
            }
            return setCookieHeader.substring(startIndex, endIndex);
        }

        private void authenticate(String username, String password) throws Exception {
                // Uniformly treat blank/null username or password as invalid credentials
                if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                        throw new AppException(ErrorCode.INVALID_CREDENTIALS);
                }
                try {
                        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                } catch (DisabledException e) {
                        log.error("User {} disabled", username);
                        throw new AppException(ErrorCode.USER_DISABLED);
                } catch (UsernameNotFoundException e) {
                        log.error("User not found: {}", username);
                        // Treat as invalid credentials for security - don't leak username existence
                        throw new AppException(ErrorCode.INVALID_CREDENTIALS);
                } catch (BadCredentialsException e) {
                        log.error("Invalid credentials for user {}", username);
                        throw new AppException(ErrorCode.INVALID_CREDENTIALS);
                }
        }

        /**
         * Lấy affiliateId từ cookie
         * Ưu tiên lấy từ affiliate_ref (chứa trực tiếp affiliateId)
         * Nếu không có, lấy từ affiliate_link_id và query database để lấy affiliateId
         */
        private Long getAffiliateIdFromCookie(HttpServletRequest request) {
            try {
                // Ưu tiên: lấy từ affiliate_ref (chứa trực tiếp affiliateId)
                String affiliateRefStr = cookieUtil.getAffiliateIdFromCookie(request);
                if (affiliateRefStr != null && !affiliateRefStr.trim().isEmpty()) {
                    try {
                        Long affiliateId = Long.parseLong(affiliateRefStr.trim());
                        log.info("Found affiliateId {} from cookie affiliate_ref", affiliateId);
                        return affiliateId;
                    } catch (NumberFormatException e) {
                        log.warn("Invalid affiliate_ref format in cookie: {}", affiliateRefStr);
                    }
                }
                
                // Fallback: lấy từ affiliate_link_id và query database
                String affiliateLinkIdStr = cookieUtil.getAffiliateLinkIdFromCookie(request);
                if (affiliateLinkIdStr != null && !affiliateLinkIdStr.trim().isEmpty()) {
                    try {
                        Long affiliateLinkId = Long.parseLong(affiliateLinkIdStr.trim());
                        return affiliateLinkRepository.findById(affiliateLinkId)
                                .map(link -> {
                                    log.info("Found affiliateId {} from affiliate_link_id {}", link.getAffiliateId(), affiliateLinkId);
                                    return link.getAffiliateId();
                                })
                                .orElse(null);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid affiliate_link_id format in cookie: {}", affiliateLinkIdStr);
                    }
                }
                
                return null;
            } catch (Exception e) {
                log.error("Error getting affiliateId from cookie: {}", e.getMessage(), e);
                return null;
            }
        }

        @Operation(summary = "Logout", description = "Deletes JWT cookie to logout user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logged out successfully"),
                        @ApiResponse(responseCode = "default", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @PostMapping("/logout")
        public ResponseEntity<?> logout(HttpServletResponse response) {
                log.info("User logout request");
                
                // Xóa cookie - PHẢI được gọi TRƯỚC khi return
                // Kiểm tra response chưa bị committed
                if (response.isCommitted()) {
                    log.error("Response already committed before deleting cookie for logout");
                } else {
                    try {
                        cookieUtil.deleteJwtCookie(response);
                        log.info("JWT cookie deleted successfully for logout");
                        
                        // Verify cookie header đã được set
                        String setCookieHeader = response.getHeader("Set-Cookie");
                        if (setCookieHeader != null) {
                            log.info("Set-Cookie header verified in logout response: cookie deleted successfully");
                        } else {
                            log.warn("Set-Cookie header not found in logout response after deleting cookie!");
                        }
                    } catch (Exception e) {
                        log.error("Failed to delete JWT cookie for logout: {}", e.getMessage(), e);
                    }
                }
                
                // Trả về ApiResponse - GlobalControllerAdvice sẽ không wrap vì đã là ApiResponse
                return ResponseEntity.ok(com.hth.udecareer.model.response.ApiResponse.success("Đăng xuất thành công"));
        }

        @Operation(
                summary = "Get Google auth URL", 
                description = """
                        **Lấy URL đăng nhập Google OAuth 2.0**
                        
                        API này tạo ra một URL để redirect người dùng đến trang đăng nhập Google.
                        
                        **Flow:**
                        1. Client gọi API này để lấy authorizationUrl (có thể truyền affiliateId)
                        2. Redirect người dùng đến URL đó
                        3. Sau khi đăng nhập Google, user sẽ được redirect về callback URL
                        4. Server xử lý callback và trả về JWT token
                        
                        **Affiliate ID:**
                        - **Web:** Có thể truyền qua parameter hoặc để middleware set cookie
                        - **Mobile App:** Truyền qua parameter `affiliateId`
                        - Affiliate ID sẽ được encode vào state parameter và lưu khi tạo user mới
                        
                        **Dành cho:** Cả Web và Mobile applications
                        """
        )
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "URL được tạo thành công"),
                        @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
        })
        @GetMapping("/auth/google/login")
        public ResponseEntity<Map<String, String>> getGoogleAuthUrl(
                @io.swagger.v3.oas.annotations.Parameter(
                        description = "Nền tảng gọi: 'mobile' hoặc 'web'",
                        example = "mobile"
                )
                @RequestParam(name = "platform", defaultValue = "web") String platform,
                @io.swagger.v3.oas.annotations.Parameter(
                        description = "ID của affiliate nếu đăng ký thông qua affiliate link (hỗ trợ cả app và web)",
                        example = "123"
                )
                @RequestParam(name = "affiliateId", required = false) Long affiliateId
        ) {
            log.info("Request for Google auth URL for platform: {}, affiliateId: {}", platform, affiliateId);

            // Encode platform và affiliateId vào state: "platform|affiliateId" hoặc chỉ "platform"
            String state = encodeState(platform, affiliateId);
            String authUrl = googleAuthService.generateGoogleAuthUrl(state);

            Map<String, String> response = new HashMap<>();
            response.put("authorizationUrl", authUrl);
            return ResponseEntity.ok(response);
        }

      
        private String encodeState(String platform, Long affiliateId) {
            if (affiliateId != null) {
                return platform + "|" + affiliateId;
            }
            return platform;
        }

     
        private String[] parseState(String state) {
            if (StringUtils.isBlank(state)) {
                return new String[]{"web", null};
            }
            String[] parts = state.split("\\|", 2);
            if (parts.length == 2) {
                return new String[]{parts[0], parts[1]};
            }
            return new String[]{parts[0], null};
        }

    @Operation(
            summary = "Google auth callback",
            description = """
                        **Xử lý callback từ Google sau khi xác thực**
                        
                        API này được Google gọi tự động sau khi người dùng đăng nhập thành công.
                        
                        **Flow:**
                        1. Người dùng đăng nhập Google thành công
                        2. Google redirect về URL này với authorization code và state
                        3. Server parse state để lấy platform và affiliateId
                        4. Server đổi code lấy user info từ Google
                        5. Tạo hoặc tìm user trong database (lưu affiliateId nếu có)
                        6. Trả về JWT token
                        
                        **Affiliate ID handling:**
                        - **Web & Mobile:** Lấy từ state parameter (được encode khi gọi /auth/google/login)
                        
                        **Dành cho:** Cả Web và Mobile applications (callback URL)
                        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập Google thành công, JWT được trả về", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Code Google không hợp lệ hoặc lỗi callback"),
            @ApiResponse(responseCode = "default", description = "Lỗi không mong đợi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.hth.udecareer.model.response.ApiResponse.class)))
    })
    @GetMapping("/auth/google/callback")
    public ResponseEntity<?> handleGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam(name = "state", required = false) String state,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
           
            String[] stateParts = parseState(state);
            String platform = stateParts[0];
            String affiliateIdFromState = stateParts[1];
            
            log.info("Google OAuth callback received for platform: {}, affiliateId from state: {}", platform, affiliateIdFromState);
            
         
            Long affiliateId = null;
            if (affiliateIdFromState != null && !affiliateIdFromState.isEmpty()) {
                try {
                    affiliateId = Long.parseLong(affiliateIdFromState);
                    log.info("Found affiliateId from state: {}", affiliateId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid affiliateId format in state: {}", affiliateIdFromState);
                }
            }
            
            // Nếu không có affiliateId từ state, lấy từ cookie affiliate_ref
            if (affiliateId == null) {
                affiliateId = getAffiliateIdFromCookie(request);
                if (affiliateId != null) {
                    log.info("Found affiliateId from cookie: {}", affiliateId);
                }
            }
            
           
            User user = affiliateId != null 
                ? googleAuthService.processGoogleCallback(code, affiliateId)
                : googleAuthService.processGoogleCallback(code);


            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(user.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            log.info("JWT token generated for Google OAuth user: {}", user.getEmail());

            if (response.isCommitted()) {
                log.error("Response already committed before setting cookie for Google OAuth");
            } else {
                try {
                    cookieUtil.addJwtCookie(response, token);
                    log.info("JWT cookie set successfully for Google OAuth user: {}", user.getEmail());
                    
                    String setCookieHeader = response.getHeader("Set-Cookie");
                    if (setCookieHeader != null) {
                       
                        String tokenInCookie = extractTokenFromSetCookie(setCookieHeader);
                        if (tokenInCookie != null && tokenInCookie.equals(token)) {
                            log.info("Set-Cookie header verified in Google OAuth callback: token matches (length: {})", token.length());
                        } else {
                            log.warn("Set-Cookie header token mismatch in Google OAuth! Expected length: {}, Got length: {}", 
                                token.length(), tokenInCookie != null ? tokenInCookie.length() : 0);
                        }
                    } else {
                        log.warn("Set-Cookie header not found in Google OAuth callback after setting cookie!");
                    }
                } catch (Exception e) {
                    log.error("Failed to set JWT cookie for Google OAuth user: {}, error: {}", 
                        user.getEmail(), e.getMessage(), e);
                  
                }
            }

            String finalRedirectUrl;
            if ("mobile".equals(platform)) {
                finalRedirectUrl = googleOAuthConfig.getMobileSuccessUrl();
            } else {
                finalRedirectUrl = googleOAuthConfig.getWebSuccessUrl();
            }

            long maxAge = cookieUtil.getCookieMaxAge();

            String redirectUrlWithParams = UriComponentsBuilder
                    .fromUriString(finalRedirectUrl)
                    .queryParam("code", code)
                    .queryParam("state", platform != null ? platform : "web")
                    .queryParam("token", token)
                    .queryParam("maxAge", maxAge) 
                    .build().toUriString();

            log.info("Redirecting to Frontend Proxy: {}", redirectUrlWithParams);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrlWithParams));
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 Redirect

        } catch (Exception e) {
            log.error("Lỗi trong quá trình xử lý callback của Google: {}", e.getMessage(), e);

            String platform = "web";
            try {
                String[] stateParts = parseState(state);
                platform = stateParts[0];
            } catch (Exception ex) {
                log.warn("Could not parse state in error handler, using default platform: web");
            }

            String errorUrl = "mobile".equals(platform) ?
                    googleOAuthConfig.getMobileSuccessUrl() :
                    googleOAuthConfig.getWebSuccessUrl();

            String redirectUrlWithError = UriComponentsBuilder
                    .fromUriString(errorUrl)
                    .queryParam("error", "authentication_failed")
                    .queryParam("error_description", e.getMessage())
                    .queryParam("state", platform)
                    .build().toUriString();

            log.error("Redirecting to error URL: {}", redirectUrlWithError);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrlWithError));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }
}