package com.hth.udecareer.repository;

import com.hth.udecareer.entities.TermFeedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermFeedRepository extends JpaRepository<TermFeedEntity, Long> {

    List<TermFeedEntity> findByPostId(Long postId);

    List<TermFeedEntity> findByTermId(Long termId);

    void deleteByPostId(Long postId);
}

