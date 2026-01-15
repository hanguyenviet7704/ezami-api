package com.hth.udecareer.repository;

import com.hth.udecareer.entities.SpaceUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceUserRepository extends JpaRepository<SpaceUserEntity, Long> {

    Optional<SpaceUserEntity> findBySpaceIdAndUserId(Long spaceId, Long userId);

    List<SpaceUserEntity> findBySpaceIdAndStatus(Long spaceId, String status);

    List<SpaceUserEntity> findByUserId(Long userId);

    Boolean existsBySpaceIdAndUserIdAndStatus(Long spaceId, Long userId, String status);

    @Query("SELECT su.spaceId FROM SpaceUserEntity su WHERE su.userId = :userId AND su.status = 'active'")
    List<Long> findJoinedSpaceIds(@Param("userId") Long userId);
}

