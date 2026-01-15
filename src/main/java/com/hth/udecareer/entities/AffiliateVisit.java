package com.hth.udecareer.entities;

import com.hth.udecareer.converter.DeviceTypeConverter;
import com.hth.udecareer.enums.DeviceType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wp_affiliate_visits")
public class AffiliateVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long id;

    @Column(name = "affiliate_id", nullable = false)
    private Long affiliateId;

    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "referrer_url", columnDefinition = "TEXT")
    private String referrerUrl;

    @Column(name = "landing_url", columnDefinition = "TEXT")
    private String landingUrl;

    @Column(name = "campaign", length = 255)
    private String campaign;

    @Column(name = "medium", length = 100)
    private String medium;

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "device_type", length = 20)
    @Convert(converter = DeviceTypeConverter.class)
    private DeviceType deviceType = DeviceType.UNKNOWN;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "is_unique")
    private Boolean isUnique = true;

    @Column(name = "is_converted")
    private Boolean isConverted = false;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "referral_id")
    private Long referralId;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "cookie_value", length = 255)
    private String cookieValue;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}




