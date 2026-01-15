package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "wp_terms")
public class TermEntity {

    @Id
    @Column(name = "term_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String name;

    protected String slug;

    @Column(name = "term_group")
    protected Long termGroup;
}
