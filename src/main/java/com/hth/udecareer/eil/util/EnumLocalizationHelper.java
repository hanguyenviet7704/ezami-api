package com.hth.udecareer.eil.util;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Utility class for localizing enum values based on Accept-Language header.
 *
 * Usage in enum classes:
 * <pre>
 * public String getLocalizedName() {
 *     return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
 * }
 * </pre>
 */
public class EnumLocalizationHelper {

    private EnumLocalizationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Get localized value based on current locale from LocaleContextHolder.
     *
     * @param englishValue English value
     * @param vietnameseValue Vietnamese value
     * @return Localized value based on current locale
     */
    public static String getLocalizedValue(String englishValue, String vietnameseValue) {
        String currentLanguage = LocaleContextHolder.getLocale().getLanguage();

        if ("en".equals(currentLanguage)) {
            return englishValue != null ? englishValue : vietnameseValue;
        } else {
            // Default to Vietnamese for "vi" or any other language
            return vietnameseValue != null ? vietnameseValue : englishValue;
        }
    }

    /**
     * Check if current locale is English.
     *
     * @return true if current locale is English
     */
    public static boolean isEnglish() {
        return "en".equals(LocaleContextHolder.getLocale().getLanguage());
    }

    /**
     * Check if current locale is Vietnamese.
     *
     * @return true if current locale is Vietnamese
     */
    public static boolean isVietnamese() {
        return "vi".equals(LocaleContextHolder.getLocale().getLanguage());
    }

    /**
     * Get current language code.
     *
     * @return Current language code (e.g., "en", "vi")
     */
    public static String getCurrentLanguage() {
        return LocaleContextHolder.getLocale().getLanguage();
    }
}
