package com.hth.udecareer.entities;

import com.hth.udecareer.enums.CommissionStatus;
import com.hth.udecareer.enums.CommissionType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wp_affiliate_referrals")
public class AffiliateReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referral_id")
    private Long id;

    @Column(name = "affiliate_id", nullable = false)
    private Long affiliateId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Convert(converter = com.hth.udecareer.converter.CommissionTypeConverter.class)
    @Column(name = "commission_type", length = 20, nullable = false)
    private CommissionType commissionType = CommissionType.PERCENTAGE;

    @Column(name = "commission_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "commission_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Convert(converter = com.hth.udecareer.converter.CommissionStatusConverter.class)
    @Column(name = "status", length = 20, nullable = false)
    private CommissionStatus status = CommissionStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payout_id")
    private Long payoutId;

    @Column(name = "order_status", length = 50)
    private String orderStatus;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "product_ids", columnDefinition = "TEXT")
    private String productIds;

    @Column(name = "product_names", columnDefinition = "TEXT")
    private String productNames;

    @Column(name = "metadata", columnDefinition = "LONGTEXT")
    private String metadata;

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




