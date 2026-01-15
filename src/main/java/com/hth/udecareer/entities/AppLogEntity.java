package com.hth.udecareer.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ez_app_log")
public class AppLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_code", nullable = false, length = 50)
    private String appCode;

    @Column(name = "device_os", nullable = false, length = 50)
    private String deviceOs;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;


}
