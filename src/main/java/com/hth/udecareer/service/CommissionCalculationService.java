package com.hth.udecareer.service;

import com.hth.udecareer.entities.AffiliateCommissionRule;
import com.hth.udecareer.enums.CommissionType;
import com.hth.udecareer.enums.RuleType;
import com.hth.udecareer.repository.AffiliateCommissionRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service để tính toán commission cho affiliate dựa trên rules
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionCalculationService {

    private final AffiliateCommissionRuleRepository commissionRuleRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculateCommission(Long affiliateId,
                                          BigDecimal orderAmount,
                                          Long productId,
                                          Long categoryId) {
        log.info("Calculating commission for affiliate {}, order amount: {}, product: {}, category: {}",
                 affiliateId, orderAmount, productId, categoryId);

        // Lấy tất cả rules active
        List<AffiliateCommissionRule> allRules = commissionRuleRepository.findByIsActiveTrueOrderByPriorityDesc();

        // Lọc rules phù hợp với context
        List<AffiliateCommissionRule> applicableRules = filterApplicableRules(
            allRules, affiliateId, productId, categoryId, orderAmount
        );

        if (applicableRules.isEmpty()) {
            log.warn("No applicable commission rules found for affiliate {}", affiliateId);
            return BigDecimal.ZERO;
        }

        // Lấy rule có priority cao nhất
        AffiliateCommissionRule selectedRule = applicableRules.get(0);

        log.info("Selected rule: {} (priority: {})", selectedRule.getRuleName(), selectedRule.getPriority());

        // Tính commission dựa trên rule
        BigDecimal commission = calculateByRule(selectedRule, orderAmount);

        log.info("Calculated commission: {} for affiliate {}", commission, affiliateId);
        return commission;
    }

    /**
     * Lọc các rules áp dụng được
     */
    private List<AffiliateCommissionRule> filterApplicableRules(
            List<AffiliateCommissionRule> rules,
            Long affiliateId,
            Long productId,
            Long categoryId,
            BigDecimal orderAmount) {

        LocalDateTime now = LocalDateTime.now();

        return rules.stream()
            .filter(rule -> {
                // Check time validity
                if (rule.getValidFrom() != null && now.isBefore(rule.getValidFrom())) {
                    return false;
                }
                if (rule.getValidUntil() != null && now.isAfter(rule.getValidUntil())) {
                    return false;
                }

                // Check minimum order amount
                if (rule.getMinOrderAmount() != null &&
                    orderAmount.compareTo(rule.getMinOrderAmount()) < 0) {
                    return false;
                }

                // Check rule type matching
                return isRuleMatching(rule, affiliateId, productId, categoryId);
            })
            .collect(Collectors.toList());
    }

    /**
     * Kiểm tra rule có match với context không
     */
    private boolean isRuleMatching(AffiliateCommissionRule rule,
                                   Long affiliateId,
                                   Long productId,
                                   Long categoryId) {
        switch (rule.getRuleType()) {
            case GLOBAL:
                return true;

            case AFFILIATE:
                return rule.getAffiliateId() != null &&
                       rule.getAffiliateId().equals(affiliateId);

            case PRODUCT:
                return rule.getProductId() != null &&
                       rule.getProductId().equals(productId);

            case CATEGORY:
                return rule.getCategoryId() != null &&
                       rule.getCategoryId().equals(categoryId);

            case TIER:
                log.warn("TIER rule type not implemented yet");
                return false;

            default:
                return false;
        }
    }

    private BigDecimal calculateByRule(AffiliateCommissionRule rule, BigDecimal orderAmount) {
        BigDecimal commission;

        switch (rule.getCommissionType()) {
            case PERCENTAGE:
                commission = calculatePercentageCommission(rule, orderAmount);
                break;

            case FIXED:
                commission = calculateFixedCommission(rule);
                break;

            case HYBRID:
                commission = calculateHybridCommission(rule, orderAmount);
                break;

            default:
                log.error("Unknown commission type: {}", rule.getCommissionType());
                return BigDecimal.ZERO;
        }

        // Apply max cap if exists
        if (rule.getMaxCommissionAmount() != null &&
            commission.compareTo(rule.getMaxCommissionAmount()) > 0) {
            log.info("Commission {} exceeds max {}, capping",
                     commission, rule.getMaxCommissionAmount());
            commission = rule.getMaxCommissionAmount();
        }

        return commission.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentageCommission(AffiliateCommissionRule rule,
                                                     BigDecimal orderAmount) {
        BigDecimal rate = rule.getCommissionRate();
        BigDecimal commission = orderAmount.multiply(rate)
                                           .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        log.debug("Percentage commission: {} * {}% = {}", orderAmount, rate, commission);
        return commission;
    }

    private BigDecimal calculateFixedCommission(AffiliateCommissionRule rule) {
        BigDecimal fixed = rule.getFixedAmount() != null ?
                          rule.getFixedAmount() : BigDecimal.ZERO;

        log.debug("Fixed commission: {}", fixed);
        return fixed;
    }

    private BigDecimal calculateHybridCommission(AffiliateCommissionRule rule,
                                                 BigDecimal orderAmount) {
        BigDecimal fixedPart = rule.getFixedAmount() != null ?
                              rule.getFixedAmount() : BigDecimal.ZERO;

        BigDecimal percentagePart = orderAmount.multiply(rule.getCommissionRate())
                                               .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal total = fixedPart.add(percentagePart);

        log.debug("Hybrid commission: {} (fixed) + {} ({}%) = {}",
                  fixedPart, percentagePart, rule.getCommissionRate(), total);

        return total;
    }

    @Transactional(readOnly = true)
    public AffiliateCommissionRule getApplicableRule(Long affiliateId,
                                                     BigDecimal orderAmount,
                                                     Long productId,
                                                     Long categoryId) {
        List<AffiliateCommissionRule> allRules = commissionRuleRepository.findByIsActiveTrueOrderByPriorityDesc();

        List<AffiliateCommissionRule> applicableRules = filterApplicableRules(
            allRules, affiliateId, productId, categoryId, orderAmount
        );

        return applicableRules.isEmpty() ? null : applicableRules.get(0);
    }
}

