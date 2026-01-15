package com.hth.udecareer.repository;

import com.hth.udecareer.entities.AffiliateVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AffiliateVisitRepository extends JpaRepository<AffiliateVisit, Long> {

    // Affiliate-level stats
    Long countByAffiliateId(Long affiliateId);

    @Query("SELECT MAX(v.createdAt) FROM AffiliateVisit v WHERE v.affiliateId = :affiliateId")
    LocalDateTime findLastVisitDateByAffiliateId(@Param("affiliateId") Long affiliateId);

    // Link-level stats
    Long countByLinkId(Long linkId);

    Long countByLinkIdAndIsUnique(Long linkId, Boolean isUnique);

    Long countByLinkIdAndIsConverted(Long linkId, Boolean isConverted);

    @Query("SELECT COUNT(v) FROM AffiliateVisit v WHERE v.linkId = :linkId AND v.createdAt >= :startDate")
    Long countByLinkIdAndCreatedAtAfter(@Param("linkId") Long linkId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(v) FROM AffiliateVisit v WHERE v.linkId = :linkId AND v.isConverted = true AND v.createdAt >= :startDate")
    Long countConversionsByLinkIdAndCreatedAtAfter(@Param("linkId") Long linkId, @Param("startDate") LocalDateTime startDate);

    // Top sources
    @Query("SELECT v.source, COUNT(v) as clickCount FROM AffiliateVisit v WHERE v.linkId = :linkId AND v.source IS NOT NULL GROUP BY v.source ORDER BY clickCount DESC")
    List<Object[]> findTopSourcesByLinkId(@Param("linkId") Long linkId);

    // Device stats
    @Query("SELECT v.deviceType, COUNT(v) FROM AffiliateVisit v WHERE v.linkId = :linkId GROUP BY v.deviceType")
    List<Object[]> countByLinkIdGroupByDeviceType(@Param("linkId") Long linkId);

    // Geographic stats
    @Query("SELECT v.country, COUNT(v) as clickCount FROM AffiliateVisit v WHERE v.linkId = :linkId AND v.country IS NOT NULL GROUP BY v.country ORDER BY clickCount DESC")
    List<Object[]> findTopCountriesByLinkId(@Param("linkId") Long linkId);

    // Unique visit detection (IP + User Agent + Time window)
    boolean existsByLinkIdAndIpAddressAndUserAgentAndCreatedAtAfter(
            Long linkId,
            String ipAddress,
            String userAgent,
            LocalDateTime after
    );

    // Conversion count by source
    @Query("SELECT v.source, COUNT(v) FROM AffiliateVisit v WHERE v.linkId = :linkId AND v.isConverted = true AND v.source IS NOT NULL GROUP BY v.source")
    List<Object[]> countConversionsByLinkIdGroupBySource(@Param("linkId") Long linkId);

    // Conversion count by country
    @Query("SELECT v.country, COUNT(v) FROM AffiliateVisit v WHERE v.linkId = :linkId AND v.isConverted = true AND v.country IS NOT NULL GROUP BY v.country")
    List<Object[]> countConversionsByLinkIdGroupByCountry(@Param("linkId") Long linkId);



    // Click report with filters
    @Query("SELECT v FROM AffiliateVisit v WHERE v.affiliateId = :affiliateId " +
            "AND (:startDate IS NULL OR v.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR v.createdAt <= :endDate) " +
            "AND (:clickId IS NULL OR v.id = :clickId) " +
            "AND (:subId IS NULL OR v.sessionId LIKE CONCAT('%', :subId, '%') OR v.cookieValue LIKE CONCAT('%', :subId, '%')) " +
            "AND (:clickArea IS NULL OR v.country LIKE CONCAT('%', :clickArea, '%') OR v.city LIKE CONCAT('%', :clickArea, '%')) " +
            "ORDER BY v.createdAt DESC")
    org.springframework.data.domain.Page<AffiliateVisit> findClicksWithFilters(
            @Param("affiliateId") Long affiliateId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("clickId") Long clickId,
            @Param("subId") String subId,
            @Param("clickArea") String clickArea,
            org.springframework.data.domain.Pageable pageable);
}

