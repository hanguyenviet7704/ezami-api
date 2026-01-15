package com.hth.udecareer.repository;

import com.hth.udecareer.entities.AffiliateLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, Long> {

    /**
     * Check if a short URL (slug) already exists in the database
     * @param shortUrl The short URL to check
     * @return true if exists, false otherwise
     */
    boolean existsByShortUrl(String shortUrl);

    /**
     * Find an affiliate link by its short URL (slug)
     * @param shortUrl The short URL to search for
     * @return Optional containing the AffiliateLink if found
     */
    Optional<AffiliateLink> findByShortUrl(String shortUrl);

    /**
     * Find all links belonging to an affiliate with pagination
     * @param affiliateId The affiliate ID
     * @param pageable Pagination info
     * @return Page of AffiliateLinks
     */
    @Query("SELECT al FROM AffiliateLink al WHERE al.affiliateId = :affiliateId")
    Page<AffiliateLink> findByAffiliateId(@Param("affiliateId") Long affiliateId, Pageable pageable);

    /**
     * Find a link by ID and affiliate ID (for security)
     * @param linkId The link ID
     * @param affiliateId The affiliate ID
     * @return Optional containing the link if found and belongs to affiliate
     */
    Optional<AffiliateLink> findByIdAndAffiliateId(Long linkId, Long affiliateId);

 
    /**
     * Sum clicks for all links belonging to an affiliate
     * @param affiliateId The affiliate ID
     * @return Total number of clicks
     */
    @Query("SELECT COALESCE(SUM(al.totalClicks), 0) FROM AffiliateLink al WHERE al.affiliateId = :affiliateId")
    Long sumClicksByAffiliateId(@Param("affiliateId") Long affiliateId);


    /**
     * Sum total clicks by affiliate, year and month (by updatedAt)
     * @param affiliateId The affiliate ID
     * @param year The year
     * @param month The month
     * @return Total clicks for the affiliate in the year-month
     */
    @Query("SELECT COALESCE(SUM(al.totalClicks), 0) FROM AffiliateLink al WHERE al.affiliateId = :affiliateId AND YEAR(al.updatedAt) = :year AND MONTH(al.updatedAt) = :month")
    Long sumTotalClicksByAffiliateAndYearMonth(@Param("affiliateId") Long affiliateId, @Param("year") Integer year, @Param("month") Integer month);

    /**
     * Sum total clicks by affiliate and year (by updatedAt)
     * @param affiliateId The affiliate ID
     * @param year The year
     * @return Total clicks for the affiliate in the year
     */
    @Query("SELECT COALESCE(SUM(al.totalClicks), 0) FROM AffiliateLink al WHERE al.affiliateId = :affiliateId AND YEAR(al.updatedAt) = :year")
    Long sumTotalClicksByAffiliateAndYear(@Param("affiliateId") Long affiliateId, @Param("year") Integer year);

    /**
     * Sum total clicks in period by affiliate (by updatedAt)
     * @param affiliateId The affiliate ID
     * @param start Start date
     * @param end End date
     * @return Total clicks for the affiliate
     */
    @Query("SELECT COALESCE(SUM(al.totalClicks), 0) FROM AffiliateLink al WHERE al.affiliateId = :affiliateId AND al.updatedAt BETWEEN :start AND :end")
    Long sumTotalClicksInPeriodByAffiliate(@Param("affiliateId") Long affiliateId, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    /**
     * Sum total clicks in period (by updatedAt)
     * @param start Start date
     * @param end End date
     * @return Total clicks
     */
    @Query("SELECT COALESCE(SUM(al.totalClicks), 0) FROM AffiliateLink al WHERE al.updatedAt BETWEEN :start AND :end")
    Long sumTotalClicksInPeriod(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    /**
     * Sum total conversions in period (by updatedAt)
     * @param start Start date
     * @param end End date
     * @return Total conversions
     */
    @Query("SELECT COALESCE(SUM(al.totalConversions), 0) FROM AffiliateLink al WHERE al.updatedAt BETWEEN :start AND :end")
    Long sumTotalConversionsInPeriod(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}

