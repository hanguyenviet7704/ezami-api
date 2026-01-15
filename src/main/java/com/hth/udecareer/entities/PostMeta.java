package com.hth.udecareer.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "wp_postmeta")
public class PostMeta {
    @Id
    @Column(name = "meta_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    protected Long postId;

    @Column(name = "meta_key")
    protected String metaKey;

    @Column(name = "meta_value")
    protected String metaValue;
}
