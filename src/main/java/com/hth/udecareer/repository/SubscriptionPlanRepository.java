package com.hth.udecareer.repository;

import com.hth.udecareer.entities.SubscriptionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, Long> {
    // Tìm gói theo mã code
    Optional<SubscriptionPlanEntity> findByCode(String code);

    boolean existsByCode(String code);

    // Batch fetch by codes to avoid N+1 queries
    List<SubscriptionPlanEntity> findByCodeIn(Collection<String> codes);
}