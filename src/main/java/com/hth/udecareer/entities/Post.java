package com.hth.udecareer.entities;

import com.hth.udecareer.converter.PostStatusConverter;
import com.hth.udecareer.enums.PostStatus;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "wp_posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============= CONTENT =============

    @Column(name = "post_content")
    private String content;

    @Column(name = "post_title")
    private String title;

    @Column(name = "post_excerpt")
    private String excerpt;

    // ============= METADATA =============

    @Column(name = "post_date")
    private LocalDateTime date;

    @Column(name = "post_status")
    @Convert(converter = PostStatusConverter.class)
    private PostStatus status;

    @Column(name = "post_name")
    private String name;

    @Column(name = "post_type")
    private String type;

    @Column(name = "post_author")
    private Long author;

    @Column(name = "comment_count")
    private Long commentCount;

    @Column(name = "guid")
    private String guid;

    // ============= RELATIONSHIPS =============

    @ManyToOne
    @JoinColumn(name = "post_author", referencedColumnName = "id", insertable = false, updatable = false)
    private User authorUser;

    @OneToMany
    @JoinColumn(name = "post_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<PostMeta> postMetas;
}
