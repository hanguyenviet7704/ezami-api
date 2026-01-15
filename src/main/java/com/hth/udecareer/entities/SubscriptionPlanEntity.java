package com.hth.udecareer.entities;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "ez_subscription_plan")
public class SubscriptionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code; // Ví dụ: PLAN_30, PLAN_90

    @Column(name = "name")
    private String name; // Ví dụ: Gói 1 Tháng

    @Column(name = "price")
    private BigDecimal price; // Lưu giá tiền (VND)

    @Column(name = "duration_days")
    private Integer durationDays; // Ví dụ: 30, 90

    @Column(name = "description")
    private String description;
}