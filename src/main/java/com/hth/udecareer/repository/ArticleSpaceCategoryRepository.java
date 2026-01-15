package com.hth.udecareer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hth.udecareer.entities.ArticleSpaceCategoryEntity;

public interface ArticleSpaceCategoryRepository extends JpaRepository<ArticleSpaceCategoryEntity, Long> {
    @Query("SELECT asce FROM ArticleSpaceCategoryEntity asce "
            + "WHERE asce.enable = true AND asce.language = :language "
            + "ORDER BY asce.space_id, asce.order")
    List<ArticleSpaceCategoryEntity> findAllEnableByLanguage(@Param("language") String language);

    @Query("SELECT asce FROM ArticleSpaceCategoryEntity asce "
           + "WHERE asce.enable = true AND asce.space_id = :spaceId AND asce.language = :language "
           + "ORDER BY asce.space_id, asce.order")
    List<ArticleSpaceCategoryEntity> findAllEnableBySpaceAndLanguage(@Param("spaceId") Long spaceId,
                                                                     @Param("language") String language);
}
