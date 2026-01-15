package com.hth.udecareer.service;

import com.hth.udecareer.entities.QrTransaction;
import com.hth.udecareer.repository.QrTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrTransactionService {

    private final QrTransactionRepository qrTransactionRepository;

    public QrTransaction createTransaction(String bankCode, String bankAccount, String amount, String message, String createdBy, long ttlSeconds, String signatureKeyId) {
        String transactionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(ttlSeconds);
        String normalizedBankCode = bankCode == null ? null : bankCode.trim().toLowerCase();

    QrTransaction tx = QrTransaction.builder()
                .transactionId(transactionId)
        .bankCode(normalizedBankCode)
                .bankAccount(bankAccount)
                .amount(amount)
                .message(message)
                .createdBy(createdBy)
                .createdAt(now)
                .expireAt(expiry)
                .used(false)
        .signatureKeyId(signatureKeyId)
                .build();

        return qrTransactionRepository.save(tx);
    }

    public Optional<QrTransaction> findByTransactionId(String transactionId) {
        return qrTransactionRepository.findByTransactionId(transactionId);
    }

    public Optional<QrTransaction> findByBankAccountAndAmount(String bankAccount, String amount) {
        var opt = qrTransactionRepository.findFirstByBankAccountAndAmountAndUsedFalseAndExpireAtAfterOrderByCreatedAtDesc(bankAccount, amount, Instant.now());
        if (opt.isPresent()) log.debug("findByBankAccountAndAmount found txId={} for bankAccount={}, amount={}", opt.get().getTransactionId(), bankAccount, amount);
        else log.debug("findByBankAccountAndAmount: no match found for bankAccount={}, amount={}", bankAccount, amount);
        return opt;
    }

    public QrTransaction markUsed(String transactionId, String usedBy) {
        int updated = qrTransactionRepository.markUsedIfNotUsed(transactionId, usedBy);
        if (updated == 0) {
            // no-op: someone else already used it
            throw new IllegalStateException("Transaction already used or not found");
        }
        return qrTransactionRepository.findByTransactionId(transactionId).orElse(null);
    }
}
