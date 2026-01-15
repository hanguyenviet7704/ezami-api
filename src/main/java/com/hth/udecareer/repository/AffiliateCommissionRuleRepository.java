package com.hth.udecareer.repository;

import com.hth.udecareer.entities.AffiliateCommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliateCommissionRuleRepository extends JpaRepository<AffiliateCommissionRule, Long> {
    List<AffiliateCommissionRule> findByIsActiveTrueOrderByPriorityDesc();
}




