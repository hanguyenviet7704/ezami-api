package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ez_article_space_category")
public class ArticleSpaceCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Long space_id;

    @Column(name = "category_slug")
    protected String categorySlug;

    @Column(name = "category_name")
    protected String categoryName;

    protected String language;

    protected Integer order;

    protected Boolean enable;
}
