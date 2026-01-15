package com.hth.udecareer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.ArticleSpaceEntity;

@Repository
public interface ArticleSpaceRepository extends JpaRepository<ArticleSpaceEntity, Long> {

    @Query("SELECT ase FROM ArticleSpaceEntity ase "
           + "WHERE ase.enable = true and ase.appCode = :appCode ORDER BY ase.order")
    List<ArticleSpaceEntity> findAllEnableByAppCode(@Param("appCode") String appCode);
}
