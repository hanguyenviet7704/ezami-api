package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    List<UserVoucher> findByUserId(Long userId);

    Page<UserVoucher> findByUserIdOrderByReceivedAtDesc(Long userId, Pageable pageable);

    List<UserVoucher> findByUserIdAndStatus(Long userId, String status);

    Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, String voucherId);

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.userId = :userId AND uv.status = 'AVAILABLE' ORDER BY uv.receivedAt DESC")
    List<UserVoucher> findAvailableByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(uv) FROM UserVoucher uv WHERE uv.userId = :userId AND uv.status = 'AVAILABLE'")
    long countAvailableByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndVoucherId(Long userId, String voucherId);

    @Query("SELECT CASE WHEN COUNT(uv) > 0 THEN true ELSE false END FROM UserVoucher uv " +
           "WHERE uv.userId = :userId AND uv.voucherId = :voucherId AND uv.status = :status")
    boolean existsByUserIdAndVoucherIdAndStatus(@Param("userId") Long userId,
                                                 @Param("voucherId") String voucherId,
                                                 @Param("status") String status);
}
