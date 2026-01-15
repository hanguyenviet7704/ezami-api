package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ez_article_space")
public class ArticleSpaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String title;

    @Column(name = "`order`")
    protected Integer order;

    protected Boolean enable;

    @Column
    protected String appCode;
}
