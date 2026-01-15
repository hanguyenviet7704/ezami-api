package com.hth.udecareer.utils;

import com.hth.udecareer.entities.PostMeta;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Utility class để xử lý featured image (thumbnail) của Post
 */
@UtilityClass
public class PostImageUtil {

    private static final String THUMBNAIL_META_KEY = "_thumbnail_id";

    /**
     * Lấy attachment ID từ postmeta với key '_thumbnail_id'
     * 
     * @param postMetas Danh sách post meta
     * @return attachment ID hoặc null nếu không tìm thấy
     */
    @Nullable
    public static Long getThumbnailId(List<PostMeta> postMetas) {
        if (postMetas == null || postMetas.isEmpty()) {
            return null;
        }

        return postMetas.stream()
                .filter(meta -> THUMBNAIL_META_KEY.equals(meta.getMetaKey()))
                .map(PostMeta::getMetaValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(PostImageUtil::parseLong)
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);
    }

    /**
     * Parse string to Long safely
     */
    @Nullable
    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
