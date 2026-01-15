package com.hth.udecareer.repository;

import com.hth.udecareer.entities.FavoriteMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface FavoriteMetaRepository extends JpaRepository<FavoriteMetaEntity, Long> {

    /**
     * Lấy tất cả metadata cho một danh sách favorites
     * Tối ưu: 1 query thay vì N queries
     */
    @Query("SELECT m FROM FavoriteMetaEntity m " +
           "WHERE m.favoriteId IN :favoriteIds " +
           "ORDER BY m.favoriteId, m.metaKey")
    List<FavoriteMetaEntity> findByFavoriteIdIn(@Param("favoriteIds") List<Long> favoriteIds);

    Optional<FavoriteMetaEntity> findByFavoriteIdAndMetaKey(Long favoriteId, String metaKey);
}
