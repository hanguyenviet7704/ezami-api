package com.hth.udecareer.repository;

import com.hth.udecareer.entities.QrTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface QrTransactionRepository extends JpaRepository<QrTransaction, Long> {
    Optional<QrTransaction> findByTransactionId(String transactionId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE QrTransaction q SET q.used = true, q.usedAt = CURRENT_TIMESTAMP, q.usedBy = :usedBy WHERE q.transactionId = :txId AND q.used = false")
    int markUsedIfNotUsed(String txId, String usedBy);

    // Find the most recent active transaction for a given bank account and amount
    java.util.Optional<com.hth.udecareer.entities.QrTransaction> findFirstByBankAccountAndAmountAndUsedFalseAndExpireAtAfterOrderByCreatedAtDesc(String bankAccount, String amount, java.time.Instant now);
}
