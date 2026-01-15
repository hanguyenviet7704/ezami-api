package com.hth.udecareer.repository;

import com.hth.udecareer.entities.AffiliateReferral;
import com.hth.udecareer.enums.CommissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AffiliateReferralRepository extends JpaRepository<AffiliateReferral, Long> {

    @Query("SELECT COALESCE(SUM(ar.commissionAmount), 0) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND ar.status IN :statuses")
    BigDecimal sumCommissionByAffiliateAndStatuses(
            @Param("affiliateId") Long affiliateId,
            @Param("statuses") List<CommissionStatus> statuses
    );

    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND ar.status IN ('APPROVED', 'PAID')")
    Long countOrdersByAffiliate(@Param("affiliateId") Long affiliateId);

    @Query("SELECT ar FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId " +
           "ORDER BY ar.createdAt DESC")
    Page<AffiliateReferral> findByAffiliateId(
            @Param("affiliateId") Long affiliateId,
            Pageable pageable
    );

    // Referral management with filter by status
    @Query("SELECT ar FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND ar.status = :status " +
           "ORDER BY ar.createdAt DESC")
    Page<AffiliateReferral> findByAffiliateIdAndStatus(
            @Param("affiliateId") Long affiliateId,
            @Param("status") CommissionStatus status,
            Pageable pageable
    );

    // Get all referrals for statistics
    List<AffiliateReferral> findAllByAffiliateId(Long affiliateId);

    @Query("SELECT COALESCE(SUM(ar.commissionAmount), 0) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND YEAR(ar.createdAt) = :year AND MONTH(ar.createdAt) = :month AND ar.status IN :statuses")
    BigDecimal sumCommissionByAffiliateAndYearMonthAndStatuses(
            @Param("affiliateId") Long affiliateId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("statuses") List<CommissionStatus> statuses
    );

    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND YEAR(ar.createdAt) = :year AND MONTH(ar.createdAt) = :month AND ar.status IN :statuses")
    Long countOrdersByAffiliateAndYearMonthAndStatuses(
            @Param("affiliateId") Long affiliateId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("statuses") List<CommissionStatus> statuses
    );

    @Query("SELECT COALESCE(SUM(ar.commissionAmount), 0) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND YEAR(ar.createdAt) = :year AND ar.status IN :statuses")
    BigDecimal sumCommissionByAffiliateAndYearAndStatuses(
            @Param("affiliateId") Long affiliateId,
            @Param("year") Integer year,
            @Param("statuses") List<CommissionStatus> statuses
    );

    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND YEAR(ar.createdAt) = :year AND ar.status IN :statuses")
    Long countOrdersByAffiliateAndYearAndStatuses(
            @Param("affiliateId") Long affiliateId,
            @Param("year") Integer year,
            @Param("statuses") List<CommissionStatus> statuses
    );

    @Query("SELECT COALESCE(SUM(ar.commissionAmount), 0) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND ar.createdAt BETWEEN :start AND :end AND ar.status IN :statuses")
    BigDecimal sumCommissionByAffiliateAndDateRangeAndStatuses(
            @Param("affiliateId") Long affiliateId,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end,
            @Param("statuses") List<CommissionStatus> statuses
    );

    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar " +
           "WHERE ar.affiliateId = :affiliateId AND ar.createdAt BETWEEN :start AND :end AND ar.status IN :statuses")
    Long countOrdersByAffiliateAndDateRangeAndStatuses(
            @Param("affiliateId") Long affiliateId,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end,
            @Param("statuses") List<CommissionStatus> statuses
    );


}




