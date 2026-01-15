package com.hth.udecareer.repository;

import com.hth.udecareer.entities.TermTaxonomyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermTaxonomyRepository extends JpaRepository<TermTaxonomyEntity, Long> {

    List<TermTaxonomyEntity> findByTaxonomy(String taxonomy);

    List<TermTaxonomyEntity> findByTermId(Long termId);

    Optional<TermTaxonomyEntity> findByTermIdAndTaxonomy(Long termId, String taxonomy);

    @Query("SELECT tt FROM TermTaxonomyEntity tt WHERE tt.taxonomy = ?1 AND tt.parent = ?2")
    List<TermTaxonomyEntity> findByTaxonomyAndParent(String taxonomy, Long parent);

    @Query("SELECT tt FROM TermTaxonomyEntity tt JOIN tt.term t WHERE tt.taxonomy = ?1 AND t.slug LIKE %?2%")
    List<TermTaxonomyEntity> findByTaxonomyAndSlugContaining(String taxonomy, String slugPart);

    @Query("SELECT COUNT(tt) FROM TermTaxonomyEntity tt WHERE tt.taxonomy = ?1")
    Long countByTaxonomy(String taxonomy);
}
