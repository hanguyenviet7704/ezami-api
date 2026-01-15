package com.hth.udecareer.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "ez_quiz_category")
public class QuizCategoryEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    protected String code;

    @Column(name = "title")
    protected String title;

    @Column(name = "header")
    protected String header;

    @Column(name = "image_uri")
    protected String imageUri;

    @Column(name = "`order`")
    protected Integer order;

    @Column(name = "enable")
    protected Boolean enable;
}
