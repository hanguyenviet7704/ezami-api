package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserMetaRepository extends JpaRepository<UserMetaEntity, Long> {
    List<UserMetaEntity> findByUserId(Long userId);
    Optional<UserMetaEntity> findByUserIdAndMetaKey(Long userId, String metaKey);
    @Query(value = """
        SELECT COUNT(*) 
        FROM wp_usermeta 
        WHERE user_id = :userId 
        AND meta_key = 'wp_capabilities' 
        AND meta_value LIKE '%administrator%'
    """, nativeQuery = true)
    int countAdminRole(@Param("userId") Long userId);

    Optional<UserMetaEntity> findByMetaKeyAndMetaValue(String metaKey, String metaValue);

    @Modifying
    @Transactional
    @Query("update UserMetaEntity m set m.metaValue = cast((cast(m.metaValue as int) + 1) as string) where m.userId = :userId AND m.metaKey = :metaKey")
    int incrementMetaValue(@Param("userId") Long userId, @Param("metaKey") String metaKey);


    @Query(value = "SELECT " +
            "u.ID as user_id, " +
            "u.display_name as display_name, " +
            "CAST(m_point.meta_value AS UNSIGNED) as total_points, " +
            "(SELECT meta_value FROM wp_usermeta WHERE user_id = u.ID AND meta_key = 'url_image' LIMIT 1) as avatar_url " +
            "FROM wp_users u " +
            "JOIN wp_usermeta m_point ON u.ID = m_point.user_id AND m_point.meta_key = 'current_points' " +
            "ORDER BY total_points DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> findTopUsersWithDetails();

    @Query(value = "SELECT " +
            "u.ID as user_id, " +
            "u.display_name as display_name, " +
            "CAST(m_point.meta_value AS UNSIGNED) as total_points, " +
            "(SELECT meta_value FROM wp_usermeta WHERE user_id = u.ID AND meta_key = 'url_image' LIMIT 1) as avatar_url " +
            "FROM wp_users u " +
            "JOIN wp_usermeta m_point ON u.ID = m_point.user_id AND m_point.meta_key = :pointKey " +
            "ORDER BY total_points DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> findTopUsersByKey(@Param("pointKey") String pointKey);


    @Query(value = "SELECT " +
            "u.ID as user_id, " +
            "u.user_login as username, " +
            "u.display_name as display_name, " +
            "CAST(m_point.meta_value AS UNSIGNED) as total_points, " +
            "(SELECT meta_value FROM wp_usermeta WHERE user_id = u.ID AND meta_key = 'url_image' LIMIT 1) as avatar_url " +
            "FROM wp_users u " +
            "JOIN wp_usermeta m_point ON u.ID = m_point.user_id AND m_point.meta_key = :pointKey " +
            "JOIN wp_usermeta m_start ON u.ID = m_start.user_id AND m_start.meta_key = :startKey " +
            "WHERE m_start.meta_value = :startValue " +
            "ORDER BY total_points DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> findTopUsersByKeyAndStart(
            @Param("pointKey") String pointKey,
            @Param("startKey") String startKey,
            @Param("startValue") String startValue
    );

    @Query(value = "SELECT " +
            "u.ID as user_id, " +
            "u.user_login as username, " +
            "u.display_name as display_name, " +
            "CAST(m_point.meta_value AS UNSIGNED) as total_points, " +
            "(SELECT meta_value FROM wp_usermeta WHERE user_id = u.ID AND meta_key = 'url_image' LIMIT 1) as avatar_url " +
            "FROM wp_users u " +
            "JOIN wp_usermeta m_point ON u.ID = m_point.user_id AND m_point.meta_key = :pointKey " +
            "JOIN wp_usermeta m_start ON u.ID = m_start.user_id AND m_start.meta_key = :startKey " +
            "WHERE m_start.meta_value = :startValue " +
            "ORDER BY total_points DESC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findUsersByKeyAndStartWithPagination(
            @Param("pointKey") String pointKey,
            @Param("startKey") String startKey,
            @Param("startValue") String startValue,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = "SELECT COUNT(DISTINCT u.ID) " +
            "FROM wp_users u " +
            "JOIN wp_usermeta m_point ON u.ID = m_point.user_id AND m_point.meta_key = :pointKey " +
            "JOIN wp_usermeta m_start ON u.ID = m_start.user_id AND m_start.meta_key = :startKey " +
            "WHERE m_start.meta_value = :startValue",
            nativeQuery = true)
    long countUsersByKeyAndStart(
            @Param("pointKey") String pointKey,
            @Param("startKey") String startKey,
            @Param("startValue") String startValue
    );

    @Query(value = "SELECT m1.meta_value FROM wp_usermeta m1 " +
            "JOIN wp_usermeta m2 ON m1.user_id = m2.user_id " +
            "WHERE m1.user_id = :userId " +
            "AND m1.meta_key = :pointKey " +
            "AND m2.meta_key = :startKey " +
            "AND m2.meta_value = :startValue " +
            "LIMIT 1", nativeQuery = true)
    String findMyScoreByPeriod(@Param("userId") Long userId,
                               @Param("pointKey") String pointKey,
                               @Param("startKey") String startKey,
                               @Param("startValue") String startValue);

    @Query(value = "SELECT COUNT(m1.user_id) FROM wp_usermeta m1 " +
            "JOIN wp_usermeta m2 ON m1.user_id = m2.user_id " +
            "WHERE m1.meta_key = :pointKey " +
            "AND CAST(m1.meta_value AS UNSIGNED) > :myScore " +
            "AND m2.meta_key = :startKey " +
            "AND m2.meta_value = :startValue", nativeQuery = true)
    long countUsersHigherPeriod(@Param("pointKey") String pointKey,
                                @Param("myScore") long myScore,
                                @Param("startKey") String startKey,
                                @Param("startValue") String startValue);

    @Query(value = "SELECT MIN(CAST(m1.meta_value AS UNSIGNED)) FROM wp_usermeta m1 " +
            "JOIN wp_usermeta m2 ON m1.user_id = m2.user_id " +
            "WHERE m1.meta_key = :pointKey " +
            "AND CAST(m1.meta_value AS UNSIGNED) > :myScore " +
            "AND m2.meta_key = :startKey " +
            "AND m2.meta_value = :startValue", nativeQuery = true)
    Long findNextHigherScorePeriod(@Param("pointKey") String pointKey,
                                   @Param("myScore") long myScore,
                                   @Param("startKey") String startKey,
                                   @Param("startValue") String startValue);

    // Query tất cả users được giới thiệu (có referred_by)
    @Query(value = "SELECT user_id, meta_value FROM wp_usermeta WHERE meta_key = :metaKey AND meta_value IS NOT NULL AND meta_value != ''", nativeQuery = true)
    List<Object[]> findAllByMetaKey(@Param("metaKey") String metaKey);

}
