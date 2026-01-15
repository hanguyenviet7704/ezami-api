package com.hth.udecareer.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long commentId;
    private String author;
    private Long authorId;
    private String avatarUrl;
    private String content;
    private LocalDateTime date;
    private int repliesCount;
}
