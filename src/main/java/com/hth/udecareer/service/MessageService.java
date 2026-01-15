package com.hth.udecareer.service;

import com.hth.udecareer.config.LocaleConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving internationalized messages.
 *
 * This service provides methods to get localized messages based on:
 * - Current locale from LocaleContextHolder (set by Accept-Language header)
 * - Explicit locale parameter
 * - Default locale (Vietnamese)
 *
 * Usage examples:
 * - messageService.getMessage("error.not_found") - Uses current locale
 * - messageService.getMessage("error.invalid_password", new Object[]{6}) - With parameters
 * - messageService.getMessage("error.not_found", LocaleConfig.LOCALE_EN) - Explicit locale
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Get message for the given code using the current locale from LocaleContextHolder.
     *
     * @param code Message code (e.g., "error.not_found")
     * @return Localized message or the code itself if not found
     */
    public String getMessage(String code) {
        return getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get message for the given code with arguments using the current locale.
     *
     * @param code Message code (e.g., "error.invalid_password")
     * @param args Arguments for message placeholders (e.g., new Object[]{6})
     * @return Localized message with arguments substituted
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get message for the given code with a specific locale.
     *
     * @param code   Message code
     * @param locale Specific locale to use
     * @return Localized message
     */
    public String getMessage(String code, Locale locale) {
        return getMessage(code, null, locale);
    }

    /**
     * Get message for the given code with arguments and specific locale.
     *
     * @param code   Message code
     * @param args   Arguments for message placeholders
     * @param locale Specific locale to use
     * @return Localized message with arguments substituted
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        if (code == null || code.isEmpty()) {
            return "";
        }

        // Ensure we use a supported locale
        Locale effectiveLocale = LocaleConfig.isSupported(locale) ? locale : LocaleConfig.DEFAULT_LOCALE;

        try {
            return messageSource.getMessage(code, args, effectiveLocale);
        } catch (Exception e) {
            // Return the code as fallback if message not found
            return code;
        }
    }

    /**
     * Get message using language code string (e.g., "en", "vi", "en-US").
     *
     * @param code     Message code
     * @param language Language code string
     * @return Localized message
     */
    public String getMessage(String code, String language) {
        Locale locale = LocaleConfig.getSupportedLocale(language);
        return getMessage(code, null, locale);
    }

    /**
     * Get message with arguments using language code string.
     *
     * @param code     Message code
     * @param args     Arguments for message placeholders
     * @param language Language code string
     * @return Localized message with arguments substituted
     */
    public String getMessage(String code, Object[] args, String language) {
        Locale locale = LocaleConfig.getSupportedLocale(language);
        return getMessage(code, args, locale);
    }

    /**
     * Get the current locale from LocaleContextHolder.
     *
     * @return Current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Get the current language code (e.g., "vi", "en").
     *
     * @return Current language code
     */
    public String getCurrentLanguage() {
        return LocaleContextHolder.getLocale().getLanguage();
    }

    /**
     * Check if current locale is Vietnamese.
     *
     * @return true if current locale is Vietnamese
     */
    public boolean isVietnamese() {
        return "vi".equals(getCurrentLanguage());
    }

    /**
     * Check if current locale is English.
     *
     * @return true if current locale is English
     */
    public boolean isEnglish() {
        return "en".equals(getCurrentLanguage());
    }
}
