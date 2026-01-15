package com.hth.udecareer.model.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CommentRequest {
    @NotNull
    private Long postId;
    @NotNull
    @Size(max = 2000)
    private String content;
    private Long parentId;
}
