package com.hth.udecareer.repository;

import com.hth.udecareer.entities.RefundRequestEntity;
import com.hth.udecareer.enums.RefundRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequestEntity, Long> {

    Page<RefundRequestEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<RefundRequestEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByOrderIdAndStatus(Long orderId, RefundRequestStatus status);

    Optional<RefundRequestEntity> findByIdAndUserId(Long id, Long userId);
}

