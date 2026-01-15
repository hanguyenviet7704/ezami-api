package com.hth.udecareer.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum containing all error codes with their corresponding message keys.
 *
 * Message keys are resolved at runtime using MessageService to support
 * internationalization (i18n). The actual messages are stored in:
 * - messages_vi.properties (Vietnamese)
 * - messages_en.properties (English)
 *
 * The 'message' field contains the message key (e.g., "error.not_found")
 * which is resolved to the actual localized message based on Accept-Language header.
 */
@Getter
public enum ErrorCode {
    // General errors
    INVALID_KEY(9999, "error.uncategorized", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND(1000, "error.not_found", HttpStatus.NOT_FOUND),

    // Email/Password validation (1001-1012)
    INVALID_EMAIL(1001, "error.invalid_email", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1002, "error.email_required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1003, "error.password_required", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1004, "error.username_required", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_REQUIRED(1005, "error.old_password_required", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1006, "error.invalid_password", HttpStatus.BAD_REQUEST),
    INVALID_CODE(1007, "error.invalid_code", HttpStatus.UNAUTHORIZED),
    INVALID_CONFIRMATION_CODE(1008, "error.invalid_confirmation_code", HttpStatus.BAD_REQUEST),
    INVALID_ACTION_STATUS_NOT_REJECTED(1009, "error.invalid_action_status", HttpStatus.BAD_REQUEST),
    PHONE_INFO_EXISTED(1056, "error.phone_existed", HttpStatus.BAD_REQUEST),
    EMAIL_INFO_EXISTED(1057, "error.email_existed", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_INCORRECT(1010, "error.old_password_incorrect", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(1011, "error.password_incorrect", HttpStatus.BAD_REQUEST),
    EXPIRE_CONFIRMATION_CODE(1012, "error.invalid_confirmation_code", HttpStatus.BAD_REQUEST),
    INVALID_TYPE(1013, "error.validation_error", HttpStatus.BAD_REQUEST),

    // User not found errors (1014-1020)
    EMAIL_USER_NOT_FOUND(1014, "error.email_user_not_found", HttpStatus.NOT_FOUND),
    SPACE_NOT_FOUND(1015, "error.space_not_found", HttpStatus.NOT_FOUND),
    POST_NOT_FOUND(1016, "error.post_not_found", HttpStatus.NOT_FOUND),
    PURCHASE_NOT_FOUND(1017, "error.purchase_not_found", HttpStatus.NOT_FOUND),
    QUIZ_NOT_FOUND(1018, "error.quiz_not_found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND(1019, "error.email_not_found", HttpStatus.NOT_FOUND),
    RESOURCE_NOT_FOUND(1020, "error.resource_not_found", HttpStatus.NOT_FOUND),

    // Auth/Access errors (1058-1027)
    USER_DISABLED(1058, "error.user_disabled", HttpStatus.FORBIDDEN),
    ACCESS_DENIED(1021, "error.access_denied", HttpStatus.FORBIDDEN),
    DUPLICATE_RESOURCE(1022, "error.duplicate_resource", HttpStatus.CONFLICT),
    RATE_LIMIT_EXCEEDED(1023, "error.rate_limit_exceeded", HttpStatus.TOO_MANY_REQUESTS),
    DEPENDENCY_ERROR(1024, "error.dependency_error", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_CREDENTIALS(1025, "error.invalid_credentials", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1026, "error.invalid_token", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN(1027, "error.expired_token", HttpStatus.UNAUTHORIZED),

    // Google Auth errors (1028-1030)
    GOOGLE_AUTH_EMAIL_MISSING(1028, "error.google_auth_email_missing", HttpStatus.BAD_REQUEST),
    GOOGLE_TOKEN_EXCHANGE_FAILED(1029, "error.google_token_exchange_failed", HttpStatus.BAD_REQUEST),
    GOOGLE_USERINFO_FETCH_FAILED(1030, "error.google_userinfo_fetch_failed", HttpStatus.BAD_REQUEST),

    // Validation errors (1031-1047)
    VALIDATION_ERROR(1031, "error.validation_error", HttpStatus.BAD_REQUEST),
    BUSINESS_LOGIC_ERROR(1032, "error.business_logic_error", HttpStatus.BAD_REQUEST),
    INVALID_CONFIRMATION_TEXT(1033, "error.invalid_confirmation_text", HttpStatus.BAD_REQUEST),
    INVALID_TIME_LIMIT_RANGE(1034, "error.invalid_time_limit_range", HttpStatus.BAD_REQUEST),
    COURSE_NOT_FOUND(1059, "error.course_not_found", HttpStatus.NOT_FOUND),
    IMAGE_UPLOAD_FAIL(1035, "error.image_upload_fail", HttpStatus.BAD_REQUEST),
    LESSON_NOT_FOUND(1063, "error.lesson_not_found", HttpStatus.NOT_FOUND),
    IMAGE_DELETE_FAIL(1036, "error.image_delete_fail", HttpStatus.BAD_REQUEST),
    FAVORITE_NOT_FOUND(1037, "error.favorite_not_found", HttpStatus.NOT_FOUND),
    ALREADY_FAVORITED(1038, "error.already_favorited", HttpStatus.CONFLICT),
    FORBIDDEN(1039, "error.forbidden", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(1040, "error.unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_POST_TYPE(1041, "error.invalid_post_type", HttpStatus.BAD_REQUEST),
    INVALID_FAVORITE_ID(1042, "error.invalid_favorite_id", HttpStatus.BAD_REQUEST),
    INVALID_POST_ID(1043, "error.invalid_post_id", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(1044, "error.invalid_date_range", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_NUMBER(1045, "error.invalid_page_number", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_SIZE(1046, "error.invalid_page_size", HttpStatus.BAD_REQUEST),
    PAGE_SIZE_TOO_LARGE(1047, "error.page_size_too_large", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1048, "error.unauthenticated", HttpStatus.UNAUTHORIZED),

    // Cart/Plan errors (1049-1052)
    CART_EMPTY(1049, "error.cart_empty", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(1050, "error.cart_item_not_found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1051, "error.category_not_found", HttpStatus.NOT_FOUND),
    PLAN_NOT_FOUND(1052, "error.plan_not_found", HttpStatus.NOT_FOUND),

    // Device/App errors (1053-1055)
    INVALID_DEVICE_OS(1053, "error.invalid_device_os", HttpStatus.BAD_REQUEST),
    INVALID_APP_CODE(1054, "error.invalid_app_code", HttpStatus.BAD_REQUEST),
    REFERRAL_CODE_EXISTED(1055, "error.referral_code_existed", HttpStatus.BAD_REQUEST),

    // Notification errors (1060-1061)
    NOTIFICATION_SETTING_REQUIRED(1060, "error.notification_setting_required", HttpStatus.BAD_REQUEST),
    INVALID_JSON_FORMAT(1061, "error.invalid_json_format", HttpStatus.BAD_REQUEST),

    // Point errors (1099)
    POINT_ALREADY_ADDED(1099, "error.point_already_added", HttpStatus.BAD_REQUEST),

    // R2/Storage errors (1100)
    R2_NOT_CONFIGURED(1100, "error.r2_not_configured", HttpStatus.BAD_REQUEST),

    // Payment errors (1107-1113)
    PAYMENT_INIT_FAIL(1107, "error.payment_init_fail", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_TXN_REF_DUPLICATE(1101, "error.payment_txn_ref_duplicate", HttpStatus.CONFLICT),
    PAYMENT_INVALID_HASH(1102, "error.payment_invalid_hash", HttpStatus.BAD_REQUEST),
    PAYMENT_TRANSACTION_FAILED(1103, "error.payment_transaction_failed", HttpStatus.BAD_REQUEST),
    PAYMENT_MISSING_PARAMS(1104, "error.payment_missing_params", HttpStatus.BAD_REQUEST),
    PAYMENT_DB_UPDATE_FAIL(1105, "error.payment_db_update_fail", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_ORDER_NOT_FOUND(1106, "error.payment_order_not_found", HttpStatus.NOT_FOUND),

    // Time/Duration errors (1200-1202)
    INVALID_LAST_SECOND(1200, "error.invalid_last_second", HttpStatus.BAD_REQUEST),
    INVALID_DURATION(1201, "error.invalid_duration", HttpStatus.BAD_REQUEST),
    LAST_SECOND_EXCEEDS_DURATION(1202, "error.last_second_exceeds_duration", HttpStatus.BAD_REQUEST),

    // Support ticket errors (1300-1305)
    SUPPORT_TITLE_TOO_LONG(1300, "error.support_title_too_long", HttpStatus.BAD_REQUEST),
    SUPPORT_DESCRIPTION_REQUIRED(1301, "error.support_description_required", HttpStatus.BAD_REQUEST),
    SUPPORT_DESCRIPTION_LENGTH(1302, "error.support_description_length", HttpStatus.BAD_REQUEST),
    SUPPORT_IMAGES_LIMIT_EXCEEDED(1303, "error.support_images_limit", HttpStatus.BAD_REQUEST),
    SUPPORT_TICKET_NOT_FOUND(1304, "error.support_ticket_not_found", HttpStatus.NOT_FOUND),
    SUPPORT_STATUS_INVALID(1305, "error.support_status_invalid", HttpStatus.BAD_REQUEST),

    // Referral errors (2001-2005)
    REFERRAL_CODE_NOT_FOUND(2001, "error.referral_code_not_found", HttpStatus.NOT_FOUND),
    REFERRAL_ALREADY_APPLIED(2002, "error.referral_already_applied", HttpStatus.BAD_REQUEST),
    SELF_REFERRAL_NOT_ALLOWED(2003, "error.self_referral_not_allowed", HttpStatus.BAD_REQUEST),
    REFERRAL_CODE_CREATE_FAILED(2004, "error.referral_code_create_failed", HttpStatus.INTERNAL_SERVER_ERROR),
    REFERRAL_CODE_INVALID_FORMAT(2005, "error.referral_code_invalid_format", HttpStatus.BAD_REQUEST),

    // User profile validation errors (2011-2020)
    COMMENT_NOT_FOUND(2011, "error.comment_not_found", HttpStatus.NOT_FOUND),
    INVALID_FULL_NAME(2012, "error.invalid_full_name", HttpStatus.BAD_REQUEST),
    FULL_NAME_TOO_LONG(2013, "error.full_name_too_long", HttpStatus.BAD_REQUEST),
    INVALID_DISPLAY_NAME(2014, "error.invalid_display_name", HttpStatus.BAD_REQUEST),
    DISPLAY_NAME_TOO_LONG(2015, "error.display_name_too_long", HttpStatus.BAD_REQUEST),
    INVALID_NICE_NAME(2006, "error.invalid_nice_name", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_URL(2007, "error.invalid_image_url", HttpStatus.BAD_REQUEST),
    INVALID_DOB_FORMAT(2008, "error.invalid_dob_format", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(2009, "error.invalid_phone_format", HttpStatus.BAD_REQUEST),
    INVALID_REFERRAL_CODE(2010, "error.invalid_referral_code", HttpStatus.BAD_REQUEST),

    // Affiliate errors (3000-3013)
    AFFILIATE_FULL_NAME_REQUIRED(3000, "error.affiliate_full_name_required", HttpStatus.BAD_REQUEST),
    AFFILIATE_FULL_NAME_MAX_255(3001, "error.affiliate_full_name_max", HttpStatus.BAD_REQUEST),
    AFFILIATE_BANK_ACCOUNT_MAX_30(3002, "error.affiliate_bank_account_max", HttpStatus.BAD_REQUEST),
    AFFILIATE_BANK_NAME_MAX_50(3003, "error.affiliate_bank_name_max", HttpStatus.BAD_REQUEST),
    AFFILIATE_PHONE_REQUIRED(3004, "error.affiliate_phone_required", HttpStatus.BAD_REQUEST),
    AFFILIATE_PHONE_INVALID(3005, "error.affiliate_phone_invalid", HttpStatus.BAD_REQUEST),
    AFFILIATE_REJECT_REASON_REQUIRED(3010, "error.affiliate_reject_reason_required", HttpStatus.BAD_REQUEST),
    AFFILIATE_BANK_ACCOUNT_IN_USE(3012, "error.affiliate_bank_account_in_use", HttpStatus.BAD_REQUEST),
    AFFILIATE_PHONE_IN_USE(3013, "error.affiliate_phone_in_use", HttpStatus.BAD_REQUEST),

    // EIL Module errors (4000-4099)
    // Skill errors
    EIL_SKILL_NOT_FOUND(4000, "error.eil.skill_not_found", HttpStatus.NOT_FOUND),
    EIL_SKILL_INACTIVE(4001, "error.eil.skill_inactive", HttpStatus.BAD_REQUEST),
    EIL_SKILL_MAPPING_NOT_FOUND(4002, "error.eil.skill_mapping_not_found", HttpStatus.NOT_FOUND),

    // Diagnostic errors
    EIL_DIAGNOSTIC_NOT_FOUND(4010, "error.eil.diagnostic_not_found", HttpStatus.NOT_FOUND),
    EIL_DIAGNOSTIC_SESSION_NOT_FOUND(4011, "error.eil.diagnostic_session_not_found", HttpStatus.NOT_FOUND),
    EIL_DIAGNOSTIC_ALREADY_COMPLETED(4012, "error.eil.diagnostic_already_completed", HttpStatus.BAD_REQUEST),
    EIL_DIAGNOSTIC_ALREADY_IN_PROGRESS(4013, "error.eil.diagnostic_already_in_progress", HttpStatus.CONFLICT),
    EIL_DIAGNOSTIC_QUESTION_ALREADY_ANSWERED(4014, "error.eil.diagnostic_question_answered", HttpStatus.BAD_REQUEST),
    EIL_DIAGNOSTIC_NO_QUESTIONS(4015, "error.eil.diagnostic_no_questions", HttpStatus.BAD_REQUEST),
    EIL_DIAGNOSTIC_NOT_COMPLETED(4016, "error.eil.diagnostic_not_completed", HttpStatus.BAD_REQUEST),

    // Practice errors
    EIL_PRACTICE_SESSION_NOT_FOUND(4020, "error.eil.practice_session_not_found", HttpStatus.NOT_FOUND),
    EIL_PRACTICE_SESSION_COMPLETED(4021, "error.eil.practice_session_completed", HttpStatus.BAD_REQUEST),
    EIL_PRACTICE_SESSION_INACTIVE(4022, "error.eil.practice_session_inactive", HttpStatus.BAD_REQUEST),
    EIL_PRACTICE_MAX_QUESTIONS_REACHED(4023, "error.eil.practice_max_questions", HttpStatus.BAD_REQUEST),
    EIL_PRACTICE_NO_QUESTIONS_AVAILABLE(4024, "error.eil.practice_no_questions", HttpStatus.BAD_REQUEST),
    EIL_PRACTICE_QUESTION_ALREADY_ANSWERED(4025, "error.eil.practice_question_answered", HttpStatus.BAD_REQUEST),

    // Mastery errors
    EIL_MASTERY_NOT_FOUND(4030, "error.eil.mastery_not_found", HttpStatus.NOT_FOUND),
    EIL_MASTERY_ALREADY_EXISTS(4031, "error.eil.mastery_exists", HttpStatus.CONFLICT),

    // Explanation/AI errors
    EIL_EXPLANATION_NOT_FOUND(4040, "error.eil.explanation_not_found", HttpStatus.NOT_FOUND),
    EIL_EXPLANATION_GENERATION_FAILED(4041, "error.eil.explanation_failed", HttpStatus.INTERNAL_SERVER_ERROR),
    EIL_AI_SERVICE_UNAVAILABLE(4042, "error.eil.ai_unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    EIL_AI_RATE_LIMITED(4043, "error.eil.ai_rate_limited", HttpStatus.TOO_MANY_REQUESTS),
    EIL_AI_TIMEOUT(4044, "error.eil.ai_timeout", HttpStatus.GATEWAY_TIMEOUT),

    // Mock test errors
    EIL_MOCK_RESULT_NOT_FOUND(4050, "error.eil.mock_not_found", HttpStatus.NOT_FOUND),
    EIL_MOCK_ALREADY_ANALYZED(4051, "error.eil.mock_already_analyzed", HttpStatus.CONFLICT),
    EIL_MOCK_QUIZ_NOT_COMPLETED(4052, "error.eil.mock_not_completed", HttpStatus.BAD_REQUEST),

    // Question errors
    EIL_QUESTION_NOT_FOUND(4060, "error.eil.question_not_found", HttpStatus.NOT_FOUND),
    EIL_QUESTION_NO_SKILL_MAPPING(4061, "error.eil.question_no_skill", HttpStatus.BAD_REQUEST),
    EIL_INVALID_ANSWER_FORMAT(4062, "error.eil.invalid_answer", HttpStatus.BAD_REQUEST),

    // User errors
    EIL_USER_SKILL_MAP_EMPTY(4070, "error.eil.skill_map_empty", HttpStatus.NOT_FOUND),
    EIL_USER_NO_DIAGNOSTIC(4071, "error.eil.no_diagnostic", HttpStatus.BAD_REQUEST),

    // Pattern/Session errors
    INVALID_REQUEST(4080, "error.invalid_request", HttpStatus.BAD_REQUEST),
    EIL_SESSION_DATA_INVALID(4081, "error.eil.session_data_invalid", HttpStatus.BAD_REQUEST),

    // Community rate limit errors (4800-4810)
    DAILY_POST_LIMIT_EXCEEDED(4800, "error.community.daily_post_limit", HttpStatus.TOO_MANY_REQUESTS),
    DAILY_COMMENT_LIMIT_EXCEEDED(4801, "error.community.daily_comment_limit", HttpStatus.TOO_MANY_REQUESTS),
    DAILY_REACTION_LIMIT_EXCEEDED(4802, "error.community.daily_reaction_limit", HttpStatus.TOO_MANY_REQUESTS),

    // Voucher errors (5000-5010)
    VOUCHER_NOT_FOUND(5000, "error.voucher_not_found", HttpStatus.NOT_FOUND),
    VOUCHER_ALREADY_CLAIMED(5001, "error.voucher_already_claimed", HttpStatus.CONFLICT),
    VOUCHER_ALREADY_USED(5002, "error.voucher_already_used", HttpStatus.BAD_REQUEST),
    VOUCHER_EXPIRED(5003, "error.voucher_expired", HttpStatus.BAD_REQUEST),
    VOUCHER_INVALID(5004, "error.voucher_invalid", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_YET_VALID(5005, "error.voucher_not_yet_valid", HttpStatus.BAD_REQUEST),

    // Refund errors (5100-5110)
    REFUND_REQUEST_NOT_FOUND(5100, "error.refund_request_not_found", HttpStatus.NOT_FOUND),
    REFUND_ALREADY_PROCESSED(5101, "error.refund_already_processed", HttpStatus.BAD_REQUEST),
    REFUND_ORDER_NOT_ELIGIBLE(5102, "error.refund_order_not_eligible", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message; // This is now a message key for i18n lookup
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * Get the message key for i18n lookup.
     * Alias for getMessage() to make intent clearer.
     *
     * @return Message key (e.g., "error.not_found")
     */
    public String getMessageKey() {
        return this.message;
    }
}
