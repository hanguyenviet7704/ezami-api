package com.hth.udecareer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hth.udecareer.entities.AppEntity;

public interface AppRepository extends JpaRepository<AppEntity, Long> {

    @Query("SELECT ae FROM AppEntity ae "
           + "WHERE ae.enable = true "
           + "and ae.appCode = :appCode")
    Optional<AppEntity> findEnableByAppCode(@Param("appCode") String appCode);
}
