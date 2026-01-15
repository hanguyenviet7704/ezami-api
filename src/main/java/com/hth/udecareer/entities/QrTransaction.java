package com.hth.udecareer.entities;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "qr_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId; 

    private String bankCode;
    private String bankAccount;
    private String amount;
    private String message;

    private String createdBy;

    private Instant createdAt;
    private Instant expireAt;

    private boolean used;
    private Instant usedAt;
    private String usedBy;

    private String signatureKeyId;
    private String signatureKeyVersion;
}
