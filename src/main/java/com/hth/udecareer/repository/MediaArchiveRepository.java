package com.hth.udecareer.repository;

import com.hth.udecareer.entities.MediaArchiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaArchiveRepository extends JpaRepository<MediaArchiveEntity, Long> {

    Optional<MediaArchiveEntity> findByMediaKey(String mediaKey);

    List<MediaArchiveEntity> findByFeedId(Long feedId);

    List<MediaArchiveEntity> findBySubObjectId(Long subObjectId);
}

