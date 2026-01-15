package com.hth.udecareer.entities;

import com.hth.udecareer.enums.CommissionType;
import com.hth.udecareer.enums.RuleType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wp_affiliate_commission_rules")
public class AffiliateCommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Convert(converter = com.hth.udecareer.converter.RuleTypeConverter.class)
    @Column(name = "rule_type", nullable = false, length = 20)
    private RuleType ruleType = RuleType.GLOBAL;

    @Column(name = "affiliate_id")
    private Long affiliateId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "tier_level")
    private Integer tierLevel;

    @Convert(converter = com.hth.udecareer.converter.CommissionTypeConverter.class)
    @Column(name = "commission_type", nullable = false, length = 20)
    private CommissionType commissionType = CommissionType.PERCENTAGE;

    @Column(name = "commission_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "fixed_amount", precision = 15, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_commission_amount", precision = 15, scale = 2)
    private BigDecimal maxCommissionAmount;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "conditions", columnDefinition = "LONGTEXT")
    private String conditions;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}




