package com.hth.udecareer.repository;

import com.hth.udecareer.entities.AffiliatePayout;
import com.hth.udecareer.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliatePayoutRepository extends JpaRepository<AffiliatePayout, Long> {
    List<AffiliatePayout> findByAffiliateIdAndStatus(Long affiliateId, PayoutStatus status);
}




