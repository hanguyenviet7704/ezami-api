package com.hth.udecareer.repository;

import com.hth.udecareer.entities.SpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<SpaceEntity, Long> {

    Optional<SpaceEntity> findBySlug(String slug);

    List<SpaceEntity> findByParentId(Long parentId);



}
