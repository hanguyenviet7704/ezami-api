package com.hth.udecareer.entities;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Entity
@Table(name = "wp_comments")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_ID")
    private Long commentId;

    @Column(name = "comment_post_ID", nullable = false)
    private Long commentPostId;

    @Column(name = "comment_author", nullable = false)
    private String commentAuthor;

    @Column(name = "comment_author_email", nullable = false)
    private String commentAuthorEmail = "";

    @Column(name = "comment_content", nullable = false, columnDefinition = "TEXT")
    private String commentContent;

    @Column(name = "comment_date", nullable = false)
    private LocalDateTime commentDate = LocalDateTime.now();

    @Column(name = "comment_date_gmt", nullable = false)
    private LocalDateTime commentDateGmt = LocalDateTime.now(ZoneOffset.UTC);

    @Column(name = "comment_parent", nullable = false)
    private Long commentParent = 0L;

    @Column(name = "comment_approved", nullable = false)
    private String commentApproved = "1";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_type", nullable = false)
    private String commentType = "comment";
}
