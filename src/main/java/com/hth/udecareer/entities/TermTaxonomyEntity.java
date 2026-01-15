package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "wp_term_taxonomy")
public class TermTaxonomyEntity {
    @Id
    @Column(name = "term_taxonomy_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "term_id")
    private Long termId;

    private Long parent;

    private Long count;

    private String taxonomy;

    private String description;

    // Relationship to TermEntity
    @ManyToOne
    @JoinColumn(name = "term_id", referencedColumnName = "term_id", insertable = false, updatable = false)
    private TermEntity term;
}
