package com.hth.udecareer.config;

import java.util.*;

import javax.persistence.OrderBy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import com.hth.udecareer.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import javax.validation.ConstraintViolationException;
import javax.validation.ConstraintViolation;
import java.util.stream.Collectors;

import org.slf4j.MarkerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.exception.InvalidPaginationException;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice implements ResponseBodyAdvice<Object> {

    private final MessageService messageService;

    @ExceptionHandler(AppException.class)
    public ApiResponse catchAppException(HttpServletRequest request,
            HttpServletResponse response,
            AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();


        response.setStatus(httpStatus.value());

        // Get localized message based on Accept-Language header
        String localizedMessage = messageService.getMessage(errorCode.getMessageKey());

        log.warn(
                MarkerFactory.getMarker("APP_EXCEPTION"),
                "AppException: URI[{}], Query[{}], Method[{}], ErrorCode[{}], HttpStatus[{}], MessageKey[{}], LocalizedMessage[{}]",
                request.getRequestURI(),
                request.getQueryString(),
                request.getMethod(),
                errorCode.name(),
                httpStatus.value(),
                errorCode.getMessageKey(),
                localizedMessage,
                e);

        return ApiResponse.builder()
                .code(errorCode.getCode())
                .message(localizedMessage)
                .data(e.getData()) // Include additional data if present
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        org.springframework.validation.FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorKey = fieldError != null ? fieldError.getDefaultMessage() : null;

        ErrorCode errorCode = ErrorCode.INVALID_KEY; // set default neu k duoc define truoc
        Map<String, Object> attributes = null; // cac thuoc tinh vi pham

        try {
            if (errorKey != null) {

                if ("USERNAME_REQUIRED".equals(errorKey) || "PASSWORD_REQUIRED".equals(errorKey)) {
                    errorCode = ErrorCode.INVALID_CREDENTIALS;
                } else {

                    errorCode = ErrorCode.valueOf(errorKey);
                }


                if (!ex.getBindingResult().getAllErrors().isEmpty()) {
                    try {
                        ConstraintViolation constraintViolation = ex.getBindingResult().getAllErrors().get(0)
                                .unwrap(ConstraintViolation.class);
                        attributes = constraintViolation.getConstraintDescriptor().getAttributes();
                    } catch (Exception e) {

                    }
                }
            }
        } catch (IllegalArgumentException e) {

            log.debug("ErrorCode not found for validation error key: {}", errorKey);
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());

        // Get localized message based on Accept-Language header
        String localizedMessage = messageService.getMessage(errorCode.getMessageKey());
        if (Objects.nonNull(attributes)) {
            localizedMessage = mapAttribute(localizedMessage, attributes);
        }
        apiResponse.setMessage(localizedMessage);


        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }

    private static final String MIN_ATTRIBUTE = "min";

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        String localizedPrefix = messageService.getMessage("error.validation_failed");
        return ApiResponse.fail(HttpStatus.BAD_REQUEST, localizedPrefix + ": " + errorMessage);
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ApiResponse handleInvalidPagination(HttpServletRequest request,
                                              HttpServletResponse response,
                                              InvalidPaginationException e) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ApiResponse.fail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * Xử lý lỗi type mismatch (ví dụ: gửi string cho Long parameter)
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ApiResponse handleTypeMismatch(HttpServletRequest request,
                                         HttpServletResponse response,
                                         org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        Object invalidValue = ex.getValue();
        String requestUri = request.getRequestURI();
        
        log.warn("Type mismatch for parameter '{}': expected {}, got value '{}' in URI: {}", 
                 paramName, requiredType, invalidValue, requestUri);
        
        // Xác định error code phù hợp dựa trên tên parameter và URI
        ErrorCode errorCode;
        String messageKey = null;

        // Kiểm tra cụ thể cho các endpoint favorites
        if (requestUri.contains("/favorites/check/") || paramName.equalsIgnoreCase("favoritableId")) {
            errorCode = ErrorCode.INVALID_POST_ID;
            messageKey = "error.invalid_favoritable_id";
        } else if ((requestUri.contains("/favorites/") && !requestUri.endsWith("/favorites"))
                || paramName.equalsIgnoreCase("favoriteId")) {
            errorCode = ErrorCode.INVALID_FAVORITE_ID;
            messageKey = "error.invalid_favorite_id";
        } else if (paramName.toLowerCase().contains("page")) {
            errorCode = ErrorCode.INVALID_PAGE_NUMBER;
        } else if (paramName.toLowerCase().contains("size")) {
            errorCode = ErrorCode.INVALID_PAGE_SIZE;
        } else if (paramName.toLowerCase().contains("id")) {
            errorCode = ErrorCode.VALIDATION_ERROR;
            messageKey = "error.invalid_id_format";
        } else {
            errorCode = ErrorCode.VALIDATION_ERROR;
        }

        String localizedMessage = messageKey != null
                ? messageService.getMessage(messageKey)
                : messageService.getMessage(errorCode.getMessageKey());

        return ApiResponse.builder()
                .code(errorCode.getCode())
                .message(localizedMessage)
                .build();
    }

    /**
     * Xử lý lỗi missing path variable hoặc request parameter
     */
    @ExceptionHandler(org.springframework.web.bind.MissingPathVariableException.class)
    public ApiResponse handleMissingPathVariable(HttpServletRequest request,
                                                HttpServletResponse response,
                                                org.springframework.web.bind.MissingPathVariableException ex) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        log.warn("Missing path variable '{}' in URI: {}", ex.getVariableName(), request.getRequestURI());

        String localizedMessage = messageService.getMessage("error.missing_path_variable", new Object[]{ex.getVariableName()});

        return ApiResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(localizedMessage)
                .build();
    }

    /**
     * Xử lý lỗi missing request parameter
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ApiResponse handleMissingRequestParameter(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    org.springframework.web.bind.MissingServletRequestParameterException ex) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        log.warn("Missing request parameter '{}' of type {} in URI: {}",
                 ex.getParameterName(), ex.getParameterType(), request.getRequestURI());

        String localizedMessage = messageService.getMessage("error.missing_parameter", new Object[]{ex.getParameterName()});

        return ApiResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(localizedMessage)
                .build();
    }

    /**
     * Xử lý lỗi HttpMessageNotReadableException (ví dụ: JSON malformed, empty body)
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ApiResponse handleHttpMessageNotReadable(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    org.springframework.http.converter.HttpMessageNotReadableException ex) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String errorMessage = ex.getMessage();
        log.warn("HTTP message not readable in URI {}: {}", request.getRequestURI(), errorMessage);

        // Kiểm tra xem có phải là empty body không
        if (errorMessage != null && errorMessage.contains("Required request body is missing")) {
            return ApiResponse.builder()
                    .code(ErrorCode.VALIDATION_ERROR.getCode())
                    .message(messageService.getMessage("error.request_body_required"))
                    .build();
        }

        return ApiResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(messageService.getMessage("error.invalid_request_body"))
                .build();
    }

    /**
     * Xử lý NumberFormatException (khi parse number thất bại)
     */
    @ExceptionHandler(NumberFormatException.class)
    public ApiResponse handleNumberFormat(HttpServletRequest request,
                                         HttpServletResponse response,
                                         NumberFormatException ex) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        
        log.warn("Number format error in URI {}: {}", request.getRequestURI(), ex.getMessage());

        String paramName = null;
        // Try to extract param name from message: some cases show e.g. "For input string: \"abc\""
        String invalidValue = null;
        try {
            String msg = ex.getMessage();
            if (msg != null && msg.contains("For input string")) {
                int i = msg.indexOf('"');
                int j = msg.lastIndexOf('"');
                if (i >= 0 && j > i) invalidValue = msg.substring(i + 1, j);
            }
        } catch (Exception ignore) {
        }

        String requestUri = request.getRequestURI();
        // Try to get a parameter name from query/path
        if (request.getParameterMap() != null) {
            // try to find the first numeric-looking parameter with invalidValue
            for (String key : request.getParameterMap().keySet()) {
                String val = request.getParameter(key);
                if (val != null && invalidValue != null && val.equals(invalidValue)) {
                    paramName = key; break;
                }
            }
        }

        // Determine the specific error code and custom message
        ErrorCode errorCode;
        String customMessage = null;

        if (requestUri.contains("/favorites/check/")) {
            errorCode = ErrorCode.INVALID_POST_ID;
            customMessage = "Favoritable ID must be a valid number greater than 0";
        } else if (requestUri.contains("/favorites/") && !requestUri.endsWith("/favorites")) {
            errorCode = ErrorCode.INVALID_FAVORITE_ID;
            customMessage = "Favorite ID must be a valid number greater than 0";
        } else if (paramName != null && paramName.equalsIgnoreCase("favoritableId")) {
            errorCode = ErrorCode.INVALID_POST_ID;
            customMessage = "Favoritable ID must be a valid number greater than 0";
        } else if (paramName != null && paramName.equalsIgnoreCase("favoriteId")) {
            errorCode = ErrorCode.INVALID_FAVORITE_ID;
            customMessage = "Favorite ID must be a valid number greater than 0";
        } else if (paramName != null && paramName.toLowerCase().contains("page")) {
            errorCode = ErrorCode.INVALID_PAGE_NUMBER;
        } else if (paramName != null && paramName.toLowerCase().contains("size")) {
            errorCode = ErrorCode.INVALID_PAGE_SIZE;
        } else if (paramName != null && paramName.toLowerCase().contains("id")) {
            errorCode = ErrorCode.VALIDATION_ERROR;
            customMessage = "ID must be a valid number greater than 0";
        } else {
            errorCode = ErrorCode.VALIDATION_ERROR;
        }

        // Build user-friendly message with param details if available
        String message = customMessage != null ? customMessage : errorCode.getMessage();
        if (paramName != null && invalidValue != null) {
            message = String.format("Invalid number format for parameter '%s' with value '%s': expected number", paramName, invalidValue);
        } else if (invalidValue != null) {
            message = String.format("Invalid number format: '%s' (expected numeric)", invalidValue);
        }

        return ApiResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse catchAnyException(HttpServletRequest request,
            HttpServletResponse response,
            Exception e) {
        final Pair<Integer, String> status = handleAnyException(request, e);
        response.setStatus(status.getFirst());
        return ApiResponse.fail(status.getFirst(), status.getSecond());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Tự động set mã 400
    public ApiResponse handleBindException(BindException ex) {

        Map<String, String> errors = new HashMap<>();

        // 1. Lấy các lỗi của từng trường (field) (ví dụ: quizType, minTimeLimit)
        ex.getBindingResult().getFieldErrors().forEach((FieldError error) -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        // 2. Lấy các lỗi chung của cả object (ví dụ: @MinMaxTimeLimit)
        ex.getBindingResult().getGlobalErrors().forEach((ObjectError error) -> {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        });

        log.warn("BindException (Validation Failed): {}", errors);

        return ApiResponse.builder()
                .code(ErrorCode.INVALID_KEY.getCode()) // Dùng mã lỗi chung
                .message("Validation Failed")
                .data(errors) // Trả về chi tiết lỗi
                .build();
    }

    public static Pair<Integer, String> handleAnyException(HttpServletRequest request, Exception e) {
        return handleAnyException(request, e, null);
    }

    public static Pair<Integer, String> handleAnyException(@NotNull HttpServletRequest request,
            @NotNull Exception e,
            @Null Set<String> sensitiveHeaderNames) {
        try {
            final ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
            final HttpStatus status = responseStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR
                    : responseStatus.value();
            final String message = (status.value() < HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ? e.getMessage()
                    : HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();

            if (status.value() < HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                log.warn(
                        MarkerFactory.getMarker("EXCEPTION_ORIGINAL_LOCATION"),
                        "Error: URI[{}], Query[{}], Method[{}], Code[{}], Headers[{}], Message[{}]",
                        request.getRequestURI(),
                        request.getQueryString(),
                        request.getMethod(),
                        status.value(),
                        generateHeadersString(request, sensitiveHeaderNames),
                        message,
                        e);
            } else {
                log.error(
                        MarkerFactory.getMarker("EXCEPTION_ORIGINAL_LOCATION"),
                        "Unexpected Error: URI[{}], Query[{}], Method[{}], Code[{}], Headers[{}], Message[{}]",
                        request.getRequestURI(),
                        request.getQueryString(),
                        request.getMethod(),
                        status.value(),
                        generateHeadersString(request, sensitiveHeaderNames),
                        e.getMessage(),
                        e);
            }
            return Pair.of(status.value(), message);
        } catch (Exception ex) {
            log.error(ex.getMessage(), e);
            throw ex;
        }
    }

    protected static String generateHeadersString(HttpServletRequest request, Set<String> sensitiveHeaderNames) {
        final StringBuilder rsl = new StringBuilder();
        final Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            final String name = headers.nextElement();
            final String val = request.getHeader(name);
            if (val == null) {
                continue;
            }
            rsl.append(name).append(": ");
            final char[] valChars = val.toCharArray();
            if (sensitiveHeaderNames != null && sensitiveHeaderNames.contains(name)) {
                Arrays.fill(valChars, 0, valChars.length, '*');
            }
            rsl.append(valChars);
            if (headers.hasMoreElements()) {
                rsl.append('|');
            }
        }
        return rsl.toString();
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {


        String requestPath = request.getURI().getPath();
        if (requestPath.contains("/v3/api-docs") ||
                requestPath.contains("/swagger-ui") ||
                requestPath.contains("/swagger-resources") ||
                requestPath.contains("/webjars") ||
                requestPath.contains("/webhook") ||
                requestPath.startsWith("/go/")){  // Exclude affiliate redirect paths
            return body;
        }

        if (body instanceof ApiResponse) {
            return body;
        }
        return ApiResponse.success(body);
    }
}
