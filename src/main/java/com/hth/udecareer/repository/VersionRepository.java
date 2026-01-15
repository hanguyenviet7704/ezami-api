package com.hth.udecareer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.VersionEntity;

@Repository
public interface VersionRepository extends JpaRepository<VersionEntity, Long> {

    @Query("SELECT ve FROM VersionEntity ve "
           + "WHERE ve.enable = true and ve.latest = true "
           + "and ve.appCode = :appCode and ve.platform = :platform "
           + "ORDER BY ve.id")
    List<VersionEntity> getLatest(@Param("appCode") String appCode,
                                  @Param("platform") String platform);
}
