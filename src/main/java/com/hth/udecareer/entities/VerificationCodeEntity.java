package com.hth.udecareer.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.hth.udecareer.enums.VerificationCodeType;

import lombok.Data;

@Data
@Entity
@Table(name = "ez_verification_code")
public class VerificationCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    protected Long userId;

    @Column(name = "email")
    protected String email;

    @Column(name = "code")
    protected String code;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    protected VerificationCodeType type;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
}
