package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "wp_term_relationships")
public class TermRelationshipEntity {
    @EmbeddedId
    private TermRelationshipId id;

    @Column(name = "term_order")
    private Long termOrder;

    // Relationship to TermTaxonomyEntity
    @ManyToOne
    @JoinColumn(name = "term_taxonomy_id", referencedColumnName = "term_taxonomy_id", insertable = false, updatable = false)
    private TermTaxonomyEntity termTaxonomy;
}
