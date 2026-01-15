package com.hth.udecareer.repository;

import com.hth.udecareer.entities.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    // Find reports by reporter
    Page<ReportEntity> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

    // Find reports by status
    Page<ReportEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // Find reports by object type and status
    Page<ReportEntity> findByObjectTypeAndStatusOrderByCreatedAtDesc(String objectType, String status, Pageable pageable);

    // Find all reports with optional filters
    @Query("SELECT r FROM ReportEntity r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:objectType IS NULL OR r.objectType = :objectType) " +
            "ORDER BY r.createdAt DESC")
    Page<ReportEntity> findReportsWithFilters(
            @Param("status") String status,
            @Param("objectType") String objectType,
            Pageable pageable
    );

    // Check if user already reported this object
    @Query("SELECT r FROM ReportEntity r WHERE r.reporterId = :reporterId " +
            "AND r.objectId = :objectId AND r.objectType = :objectType " +
            "AND r.status IN ('pending', 'reviewing')")
    Optional<ReportEntity> findExistingReport(
            @Param("reporterId") Long reporterId,
            @Param("objectId") Long objectId,
            @Param("objectType") String objectType
    );

    // Count reports by status
    long countByStatus(String status);

    // Count reports for a specific object
    @Query("SELECT COUNT(r) FROM ReportEntity r WHERE r.objectId = :objectId AND r.objectType = :objectType")
    long countByObjectIdAndObjectType(@Param("objectId") Long objectId, @Param("objectType") String objectType);

    // Find reports for a specific object
    List<ReportEntity> findByObjectIdAndObjectTypeOrderByCreatedAtDesc(Long objectId, String objectType);

    // Find reports by reported user
    Page<ReportEntity> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId, Pageable pageable);

    // Count pending reports
    @Query("SELECT COUNT(r) FROM ReportEntity r WHERE r.status = 'pending'")
    long countPendingReports();

    // Get report statistics
    @Query("SELECT r.status, COUNT(r) FROM ReportEntity r GROUP BY r.status")
    List<Object[]> getReportStatistics();
}
