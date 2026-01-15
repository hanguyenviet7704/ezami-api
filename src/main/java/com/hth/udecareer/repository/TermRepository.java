package com.hth.udecareer.repository;

import com.hth.udecareer.entities.TermEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermRepository extends JpaRepository<TermEntity, Long> {

    Optional<TermEntity> findBySlug(String slug);

    List<TermEntity> findByNameContaining(String name);

    @Query("SELECT t FROM TermEntity t WHERE t.slug LIKE %?1%")
    List<TermEntity> findBySlugContaining(String slugPart);

    @Query("SELECT t FROM TermEntity t WHERE t.name = ?1 AND t.slug NOT LIKE '%pll_%'")
    Optional<TermEntity> findCanonicalByName(String name);
}
