package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;

@Data
@Embeddable
public class TermRelationshipId implements Serializable {
    @Serial
    private static final long serialVersionUID = 352140963071948227L;

    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "term_taxonomy_id")
    private Long termTaxonomyId;
}
