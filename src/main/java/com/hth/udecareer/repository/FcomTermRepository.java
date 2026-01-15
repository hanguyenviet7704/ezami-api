package com.hth.udecareer.repository;

import com.hth.udecareer.entities.FcomTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcomTermRepository extends JpaRepository<FcomTermEntity, Long> {

    Optional<FcomTermEntity> findBySlug(String slug);

    List<FcomTermEntity> findByTaxonomyName(String taxonomyName);
}

