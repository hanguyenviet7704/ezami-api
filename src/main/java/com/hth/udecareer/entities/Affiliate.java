package com.hth.udecareer.entities;

import com.hth.udecareer.enums.AffiliateStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import com.hth.udecareer.converter.AffiliateStatusConverter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wp_affiliates")
public class Affiliate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "affiliate_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "affiliate_code", nullable = false, unique = true, length = 50)
    private String affiliateCode;

    @Convert(converter = AffiliateStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private AffiliateStatus status = AffiliateStatus.PENDING;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "promotion_method", length = 255)
    private String promotionMethod;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "paypal_email", length = 255)
    private String paypalEmail;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "bank_account_number", length = 100)
    private String bankAccountNumber;

    @Column(name = "bank_account_name", length = 255)
    private String bankAccountName;

    @Column(name = "bank_swift_code", length = 50)
    private String bankSwiftCode;

    @Column(name = "stripe_account_id", length = 255)
    private String stripeAccountId;

    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_commissions", precision = 15, scale = 2)
    private BigDecimal totalCommissions = BigDecimal.ZERO;

    @Column(name = "paid_commissions", precision = 15, scale = 2)
    private BigDecimal paidCommissions = BigDecimal.ZERO;

    @Column(name = "unpaid_commissions", precision = 15, scale = 2)
    private BigDecimal unpaidCommissions = BigDecimal.ZERO;

    @Column(name = "total_referrals")
    private Integer totalReferrals = 0;

    @Column(name = "total_visits")
    private Integer totalVisits = 0;

    @Column(name = "total_conversions")
    private Integer totalConversions = 0;

    @Column(name = "conversion_rate", precision = 5, scale = 2)
    private BigDecimal conversionRate = BigDecimal.ZERO;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.registeredAt == null) {
            this.registeredAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}


