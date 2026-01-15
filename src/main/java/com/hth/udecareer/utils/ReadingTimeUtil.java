package com.hth.udecareer.utils;

import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;

/**
 * Utility class để tính thời gian đọc bài viết
 * 
 * Công thức: 
 * - Người đọc trung bình: 200 từ/phút
 * - reading_time = ceil(word_count / 200)
 * - Minimum: 1 phút
 */
@UtilityClass
public class ReadingTimeUtil {

    /**
     * Số từ trung bình một người đọc trong 1 phút
     */
    private static final int WORDS_PER_MINUTE = 200;

    /**
     * Tính thời gian đọc từ HTML content
     * 
     * @param htmlContent Nội dung HTML của bài viết
     * @return Số phút đọc (làm tròn lên), tối thiểu là 1 phút
     */
    public static int calculateReadingTime(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return 1;
        }

        String plainText = stripHtml(htmlContent);
        int wordCount = countWords(plainText);
        int minutes = (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);

        return Math.max(1, minutes);
    }

    /**
     * Tính thời gian đọc với custom words per minute
     * 
     * @param htmlContent Nội dung HTML
     * @param wordsPerMinute Tốc độ đọc custom (VD: 180 cho tiếng Việt)
     * @return Số phút đọc
     */
    public static int calculateReadingTime(String htmlContent, int wordsPerMinute) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return 1;
        }

        String plainText = stripHtml(htmlContent);
        int wordCount = countWords(plainText);
        int minutes = (int) Math.ceil((double) wordCount / wordsPerMinute);

        return Math.max(1, minutes);
    }

    /**
     * Lấy thông tin chi tiết về reading time
     * 
     * @param htmlContent Nội dung HTML
     * @return ReadingTimeInfo chứa word count và reading minutes
     */
    public static ReadingTimeInfo getReadingTimeInfo(String htmlContent) {
        String plainText = stripHtml(htmlContent);
        int wordCount = countWords(plainText);
        int minutes = (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);

        return ReadingTimeInfo.builder()
                .wordCount(wordCount)
                .readingMinutes(Math.max(1, minutes))
                .wordsPerMinute(WORDS_PER_MINUTE)
                .build();
    }

    // ============= PRIVATE HELPER METHODS =============

    /**
     * Loại bỏ HTML tags và giữ lại text thuần
     */
    private static String stripHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        try {
            // Parse HTML và lấy text (sử dụng Jsoup)
            String text = Jsoup.parse(html).text();
            
            // Cleanup: loại bỏ multiple spaces
            return text.replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            // Fallback: dùng regex nếu Jsoup fail
            return html.replaceAll("<[^>]*>", " ")
                      .replaceAll("\\s+", " ")
                      .trim();
        }
    }

    /**
     * Đếm số từ trong text
     */
    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] words = text.trim().split("\\s+");
        
        int count = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    // ============= DTO =============

    /**
     * DTO chứa thông tin chi tiết về reading time
     */
    @lombok.Data
    @lombok.Builder
    public static class ReadingTimeInfo {
        private int wordCount;
        private int readingMinutes;
        private int wordsPerMinute;
    }
}
