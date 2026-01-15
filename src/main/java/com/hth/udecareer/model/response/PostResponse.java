package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hth.udecareer.entities.Post;
import com.hth.udecareer.model.dto.AttachmentInfo;
import com.hth.udecareer.utils.HtmlUtil;
import com.hth.udecareer.utils.PostImageUtil;
import com.hth.udecareer.utils.ReadingTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Data
@Builder
@Schema(description = "Thông tin bài viết/article")
public class PostResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Pattern TABLE_PATTERN = Pattern.compile("(<table.*>)");

    // ============= BASIC INFO =============
    
    @Schema(description = "ID duy nhất của bài viết", example = "123")
    private Long id;

    @Schema(description = "Tiêu đề bài viết", example = "Hướng dẫn học TOEIC hiệu quả")
    private String title;

    @Schema(description = "Tên (slug) của bài viết", example = "huong-dan-hoc-toeic-hieu-qua")
    private String name;

    @Schema(description = "Nội dung HTML của bài viết", example = "<p>Nội dung bài viết...</p>")
    private String content;

    @Schema(description = "Trạng thái bài viết", example = "PUBLISH", allowableValues = {"PUBLISH", "DRAFT", "PRIVATE"})
    private String status;

    @Schema(description = "Loại bài viết", example = "post")
    private String type;

    // ============= POST METADATA =============

    @Schema(description = "Trích đoạn/tóm tắt bài viết", example = "Đây là bài viết hướng dẫn...")
    @JsonProperty("post_excerpt")
    private String postExcerpt;

    @Schema(description = "Ngày đăng bài", example = "2024-01-15T10:30:00")
    @JsonProperty("post_date")
    private LocalDateTime postDate;

    @Schema(description = "ID tác giả", example = "1")
    @JsonProperty("post_author")
    private Long postAuthor;

    @Schema(description = "Số lượng comment", example = "25")
    @JsonProperty("comment_count")
    private Long commentCount;

    // ============= AUTHOR INFO =============

    @Schema(description = "Tên hiển thị của tác giả", example = "Nguyễn Văn A")
    @JsonProperty("author_name")
    private String authorName;

    // ============= FEATURED IMAGE =============

    @Schema(description = "ID của ảnh đại diện (attachment)", example = "456")
    @JsonProperty("attachment_id")
    private Long attachmentId;

    @Schema(description = "URL ảnh đại diện", example = "https://example.com/wp-content/uploads/2024/01/image.jpg")
    @JsonProperty("image_url")
    private String imageUrl;

    @Schema(description = "Tiêu đề ảnh đại diện", example = "Hình minh họa")
    @JsonProperty("image_title")
    private String imageTitle;

    // ============= CALCULATED FIELDS =============

    @Schema(description = "Thời gian đọc ước tính (phút)", example = "5")
    @JsonProperty("reading_time")
    private Integer readingTime;

    // ============= FACTORY METHODS =============

    // ============= FACTORY METHODS =============

    /**
     * Tạo PostResponse từ Post entity (không có attachment info)
     * 
     * @param post Post entity từ database
     * @param showContent true = show full content, false = empty content
     * @return PostResponse với các field đã map
     */
    public static PostResponse from(@NotNull Post post, boolean showContent) {
        return from(post, showContent, null);
    }

    /**
     * Tạo PostResponse từ Post entity (có attachment info)
     * 
     * @param post Post entity từ database
     * @param showContent true = show full content, false = empty content
     * @param attachmentInfo Thông tin ảnh đại diện (có thể null)
     * @return PostResponse với đầy đủ các field
     */
    public static PostResponse from(@NotNull Post post, boolean showContent, AttachmentInfo attachmentInfo) {
        // Process content
        final String content = StringUtils.isBlank(post.getContent()) 
                ? "<p></p>"
                : TABLE_PATTERN.matcher(post.getContent()).replaceAll("<table>");

        // Extract thumbnail ID from postmeta
        final Long thumbnailId = PostImageUtil.getThumbnailId(post.getPostMetas());

        // Extract author name from User entity
        final String authorName = extractAuthorName(post);

        // Calculate reading time (always calculate, even if not showing content)
        final Integer readingTime = calculateReadingTime(post);

        // Build response
        return builder()
                // Basic info
                .id(post.getId())
                .title(post.getTitle())
                .name(post.getName())
                .content(showContent ? HtmlUtil.processHtml(content) : "")
                .status(post.getStatus().name())
                .type(post.getType())
                
                // Post metadata
                .postExcerpt(post.getExcerpt())
                .postDate(post.getDate())
                .postAuthor(post.getAuthor())
                .commentCount(post.getCommentCount() != null ? post.getCommentCount() : 0L)
                
                // Author info
                .authorName(authorName)
                
                // Featured image
                .attachmentId(thumbnailId)
                .imageUrl(attachmentInfo != null ? attachmentInfo.getUrl() : null)
                .imageTitle(attachmentInfo != null ? attachmentInfo.getTitle() : null)
                
                // Calculated fields
                .readingTime(readingTime)
                .build();
    }

    // ============= PRIVATE HELPER METHODS =============

    /**
     * Lấy tên hiển thị của tác giả
     */
    private static String extractAuthorName(Post post) {
        if (post.getAuthorUser() == null) {
            return null;
        }
        
        return StringUtils.isNotBlank(post.getAuthorUser().getDisplayName())
                ? post.getAuthorUser().getDisplayName()
                : post.getAuthorUser().getUsername();
    }

    /**
     * Tính thời gian đọc từ content
     */
    private static Integer calculateReadingTime(Post post) {
        if (StringUtils.isBlank(post.getContent())) {
            return null;
        }
        
        return ReadingTimeUtil.calculateReadingTime(post.getContent());
    }
}
