package com.hth.udecareer.entities;

import com.hth.udecareer.converter.LinkTypeConverter;
import com.hth.udecareer.enums.LinkType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wp_affiliate_links")
public class AffiliateLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long id;

    @Column(name = "affiliate_id", nullable = false)
    private Long affiliateId;

    @Column(name = "original_url", columnDefinition = "TEXT", nullable = false)
    private String originalUrl;

    @Column(name = "affiliate_url", columnDefinition = "TEXT", nullable = false)
    private String affiliateUrl;

    @Column(name = "short_url", length = 255)
    private String shortUrl;

    @Column(name = "pretty_url", columnDefinition = "TEXT")
    private String prettyUrl;

    @Column(name = "campaign", length = 255)
    private String campaign;

    @Column(name = "medium", length = 100)
    private String medium;

    @Column(name = "source", length = 255)
    private String source;

    @Convert(converter = LinkTypeConverter.class)
    @Column(name = "link_type", length = 20)
    private LinkType linkType = LinkType.CUSTOM;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "total_clicks")
    private Integer totalClicks = 0;

    @Column(name = "unique_clicks")
    private Integer uniqueClicks = 0;

    @Column(name = "total_conversions")
    private Integer totalConversions = 0;

    @Column(name = "total_commission", precision = 15, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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




