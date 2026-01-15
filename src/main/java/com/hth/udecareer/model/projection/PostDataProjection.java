package com.hth.udecareer.model.projection;

/**
 * Projection để lấy post data từ wp_posts và ez_quiz_category
 */
public interface PostDataProjection {
    Long getPostId();
    String getPostTitle();
    String getPostStatus();
    String getCategoryCode();
    String getCategoryTitle();
    String getCategoryImageUri();
}
