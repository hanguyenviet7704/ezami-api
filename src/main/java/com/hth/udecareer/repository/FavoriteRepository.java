package com.hth.udecareer.repository;

import com.hth.udecareer.entities.FavoriteEntity;
import com.hth.udecareer.enums.FavoriteStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {

    // ==================== Queries Without Type Filter ====================
    // Note: Database đã lưu quiz_pro_id cho QUIZ, post_id cho others - không cần convert

    @Query(value = "SELECT f.id, f.user_id, f.favoritable_type, f.favoritable_id, " +
           "f.status, f.deleted_at, f.created_at, f.updated_at " +
           "FROM ez_favorites f " +
           "WHERE f.user_id = :userId AND f.status = :statusValue " +
           "ORDER BY f.created_at DESC",
           nativeQuery = true)
    List<FavoriteEntity> findByUserIdAndStatusOptimized(
            @Param("userId") Long userId,
            @Param("statusValue") String statusValue,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM ez_favorites f " +
           "WHERE f.user_id = :userId AND f.status = :statusValue",
           nativeQuery = true)
    long countByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("statusValue") String statusValue
    );

    @Query(value = "SELECT f.id, f.user_id, f.favoritable_type, f.favoritable_id, " +
           "f.status, f.deleted_at, f.created_at, f.updated_at " +
           "FROM ez_favorites f " +
           "WHERE f.user_id = :userId AND f.status = :statusValue " +
           "AND (:fromDate IS NULL OR f.created_at >= :fromDate) " +
           "AND (:toDate IS NULL OR f.created_at <= :toDate) " +
           "ORDER BY f.created_at DESC",
           nativeQuery = true)
    List<FavoriteEntity> findByUserIdAndStatusWithDateRange(
            @Param("userId") Long userId,
            @Param("statusValue") String statusValue,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM ez_favorites f " +
           "WHERE f.user_id = :userId AND f.status = :statusValue " +
           "AND (:fromDate IS NULL OR f.created_at >= :fromDate) " +
           "AND (:toDate IS NULL OR f.created_at <= :toDate)",
           nativeQuery = true)
    long countByUserIdAndStatusWithDateRange(
            @Param("userId") Long userId,
            @Param("statusValue") String statusValue,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    // ==================== Queries With Type Filter (Native SQL) ====================
    // Note: Database đã lưu quiz_pro_id cho QUIZ, post_id cho others - không cần convert

    @Query(value = "SELECT f.id, f.user_id, f.favoritable_type, f.favoritable_id, " +
           "f.status, f.deleted_at, f.created_at, f.updated_at " +
           "FROM ez_favorites f " +
           "WHERE f.user_id = :userId " +
           "AND f.favoritable_type = :typeValue " +
           "AND f.status = :statusValue " +
           "ORDER BY f.created_at DESC",
           nativeQuery = true)
    List<FavoriteEntity> findByUserIdAndTypeAndStatusOptimized(
            @Param("userId") Long userId,
            @Param("typeValue") String typeValue,
            @Param("statusValue") String statusValue,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM ez_favorites f " +
           "WHERE f.user_id = :userId " +
           "AND f.favoritable_type = :typeValue " +
           "AND f.status = :statusValue",
           nativeQuery = true)
    long countByUserIdAndTypeAndStatus(
            @Param("userId") Long userId,
            @Param("typeValue") String typeValue,
            @Param("statusValue") String statusValue
    );

    @Query(value = "SELECT f.id, f.user_id, f.favoritable_type, f.favoritable_id, " +
           "f.status, f.deleted_at, f.created_at, f.updated_at " +
           "FROM ez_favorites f " +
           "WHERE f.user_id = :userId " +
           "AND f.favoritable_type = :typeValue " +
           "AND f.status = :statusValue " +
           "AND (:fromDate IS NULL OR f.created_at >= :fromDate) " +
           "AND (:toDate IS NULL OR f.created_at <= :toDate) " +
           "ORDER BY f.created_at DESC",
           nativeQuery = true)
    List<FavoriteEntity> findByUserIdAndTypeAndStatusWithDateRange(
            @Param("userId") Long userId,
            @Param("typeValue") String typeValue,
            @Param("statusValue") String statusValue,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM ez_favorites f " +
           "WHERE f.user_id = :userId " +
           "AND f.favoritable_type = :typeValue " +
           "AND f.status = :statusValue " +
           "AND (:fromDate IS NULL OR f.created_at >= :fromDate) " +
           "AND (:toDate IS NULL OR f.created_at <= :toDate)",
           nativeQuery = true)
    long countByUserIdAndTypeAndStatusWithDateRange(
            @Param("userId") Long userId,
            @Param("typeValue") String typeValue,
            @Param("statusValue") String statusValue,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    // ==================== Lookup Methods ====================

    @Query(value = "SELECT * FROM ez_favorites f " +
           "WHERE f.user_id = :userId " +
           "AND f.favoritable_type = :typeValue " +
           "AND f.favoritable_id = :favoritableId",
           nativeQuery = true)
    Optional<FavoriteEntity> findByUserIdAndTypeAndFavoritableId(
            @Param("userId") Long userId,
            @Param("typeValue") String typeValue,
            @Param("favoritableId") Long favoritableId
    );

    @Query(value = "SELECT * FROM ez_favorites f " +
           "WHERE f.user_id = :userId " +
           "AND f.favoritable_id = :favoritableId " +
           "AND f.status = 'active'",
           nativeQuery = true)
    Optional<FavoriteEntity> findActiveFavoriteByUserIdAndFavoritableId(
            @Param("userId") Long userId,
            @Param("favoritableId") Long favoritableId
    );
}
