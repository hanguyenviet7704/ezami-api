package com.hth.udecareer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.UserActivityMetaEntity;

@Repository
public interface UserActivityMetaRepository extends JpaRepository<UserActivityMetaEntity, Long> {

    List<UserActivityMetaEntity> findAllByActivityIdIn(List<Long> activityIds);
    Optional<UserActivityMetaEntity> findByActivityIdAndActivityMetaKey(Long activityId, String activityMetaKey);

    @Query("select uam from UserActivityMetaEntity uam where uam.activityId = :activityId")
    List<UserActivityMetaEntity> findByActivityId(@Param("activityId") Long activityId);

    @Query("select uam from UserActivityMetaEntity uam where uam.activityId = :activityId and uam.activityMetaKey = :key")
    Optional<UserActivityMetaEntity> findByActivityIdAndKey(@Param("activityId") Long activityId, @Param("key") String key);
}
