package com.hth.udecareer.entities;

import com.hth.udecareer.converter.PostStatusConverter;
import com.hth.udecareer.enums.PostStatus;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "wp_posts")
@Where(clause = "post_type = 'sfwd-courses'")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_content")
    private String content;

    @Column(name = "post_title")
    private String title;

    @Column(name = "post_date")
    private LocalDateTime date;

    @Column(name = "post_status")
    @Convert(converter = PostStatusConverter.class)
    private PostStatus status;

    @Column(name = "post_name")
    private String name;

    @Column(name = "post_type")
    private String type;

    @Column(name = "comment_count")
    private Long commentCount;

    // 1 course - n lesson
    @OneToMany
    @JoinColumn(name = "post_parent", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Lesson> lessons = new ArrayList<>();
}
