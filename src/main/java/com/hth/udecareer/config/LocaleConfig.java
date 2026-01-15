package com.hth.udecareer.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * Configuration for internationalization (i18n) support.
 *
 * Supports two languages:
 * - Vietnamese (vi) - Default language for local users
 * - English (en) - For global/international users
 *
 * The locale is determined by the Accept-Language HTTP header.
 * If no header is provided, Vietnamese is used as the default.
 */
@Configuration
public class LocaleConfig {

    public static final Locale LOCALE_VI = Locale.forLanguageTag("vi");
    public static final Locale LOCALE_EN = Locale.forLanguageTag("en");
    public static final Locale DEFAULT_LOCALE = LOCALE_VI;

    /**
     * Configure the locale resolver to use Accept-Language header.
     * Default locale is Vietnamese (vi) for local users.
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(DEFAULT_LOCALE);
        resolver.setSupportedLocales(Arrays.asList(LOCALE_VI, LOCALE_EN));
        return resolver;
    }

    /**
     * Configure the message source to load messages from properties files.
     *
     * Files loaded:
     * - messages.properties (fallback)
     * - messages_vi.properties (Vietnamese)
     * - messages_en.properties (English)
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true); // Return code if message not found
        return messageSource;
    }

    /**
     * Helper method to get supported locale from language code string.
     * Returns default locale if the language is not supported.
     *
     * @param language Language code (e.g., "vi", "en", "vi-VN", "en-US")
     * @return Supported Locale object
     */
    public static Locale getSupportedLocale(String language) {
        if (language == null || language.isEmpty()) {
            return DEFAULT_LOCALE;
        }

        // Extract primary language code (e.g., "en" from "en-US")
        String primaryLang = language.split("[-_]")[0].toLowerCase();

        switch (primaryLang) {
            case "en":
                return LOCALE_EN;
            case "vi":
            default:
                return LOCALE_VI;
        }
    }

    /**
     * Check if a locale is supported.
     *
     * @param locale Locale to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(Locale locale) {
        if (locale == null) {
            return false;
        }
        String lang = locale.getLanguage();
        return "vi".equals(lang) || "en".equals(lang);
    }
}
