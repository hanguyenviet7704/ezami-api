package com.hth.udecareer.entities;

import com.hth.udecareer.converter.PostStatusConverter;
import com.hth.udecareer.enums.PostStatus;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * LearnDash Assignment (sfwd-assignment)
 * Assignments are submissions by students for lessons/courses
 */
@Data
@Entity
@Table(name = "wp_posts")
@Where(clause = "post_type = 'sfwd-assignment'")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_author")
    private Long authorId;

    @Column(name = "post_content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "post_title")
    private String title;

    @Column(name = "post_excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "post_date")
    private LocalDateTime date;

    @Column(name = "post_modified")
    private LocalDateTime modifiedDate;

    @Column(name = "post_status")
    @Convert(converter = PostStatusConverter.class)
    private PostStatus status;

    @Column(name = "post_name")
    private String name;

    @Column(name = "post_type")
    private String type;

    @Column(name = "post_parent")
    private Long parentId;

    @Column(name = "menu_order")
    private Integer menuOrder;

    @Column(name = "comment_status")
    private String commentStatus;

    @Column(name = "comment_count")
    private Long commentCount;
}
