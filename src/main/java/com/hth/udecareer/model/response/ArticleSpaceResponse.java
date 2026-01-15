package com.hth.udecareer.model.response;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ArticleSpaceResponse implements Serializable {
    private Long id;

    private String title;

    private List<Category> categories;

    public static ArticleSpaceResponse from(Long id,
                                            String title,
                                            List<Category> categories) {
        return builder()
                .id(id)
                .title(title)
                .categories(categories)
                .build();
    }

    public static ArticleSpaceResponse from(Long id,
                                            String title) {
        return builder()
                .id(id)
                .title(title)
                .categories(List.of())
                .build();
    }

    @Data
    @Builder
    public static class Category implements Serializable {
        private Long id;

        private String name;

        public static Category from(@NotNull Long id,
                                    @NotNull String name) {
            return builder()
                    .id(id)
                    .name(name)
                    .build();
        }
    }
}
