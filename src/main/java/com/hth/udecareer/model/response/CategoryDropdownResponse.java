package com.hth.udecareer.model.response;

import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.TermEntity;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CategoryDropdownResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String code;
    private String title;
    private String description;
    private String header;
    private String imageUri;
    private Long count;
    @Builder.Default
    private  List<CategoryDropdownResponse> children = new ArrayList<>();

    public static CategoryDropdownResponse fromQuiz(QuizCategoryEntity e) {
        return CategoryDropdownResponse.builder()
                .id(e.getId())
                .code(e.getCode())
                .title(e.getTitle())
                .header(e.getHeader())
                .imageUri(e.getImageUri())
                .build();
    }

    public static CategoryDropdownResponse fromPost(TermEntity e) {
        return CategoryDropdownResponse.builder()
                .id(e.getId())
                .code(e.getSlug())
                .title(e.getName())
                .children(new ArrayList<>())
                .build();
    }
}
