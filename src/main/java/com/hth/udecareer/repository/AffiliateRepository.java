package com.hth.udecareer.repository;

import com.hth.udecareer.entities.Affiliate;
import com.hth.udecareer.enums.AffiliateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AffiliateRepository extends JpaRepository<Affiliate, Long> {
    Optional<Affiliate> findByEmail(String email);
    Optional<Affiliate> findByUserId(Long userId);
    Optional<Affiliate> findByAffiliateCode(String affiliateCode);
    boolean existsByEmail(String email);
    boolean existsByUserId(Long userId);
    boolean existsByPhoneAndEmailNot(String phone, String email);
    
    @Query("SELECT COUNT(a) > 0 FROM Affiliate a WHERE a.bankAccountNumber = :bankAccountNumber " +
           "AND ((:bankName IS NULL AND a.bankName IS NULL) OR (:bankName IS NOT NULL AND a.bankName = :bankName)) " +
           "AND a.email != :email")
    boolean existsByBankAccountNumberAndBankNameAndEmailNot(
            @Param("bankAccountNumber") String bankAccountNumber, 
            @Param("bankName") String bankName, 
            @Param("email") String email);
    
    Page<Affiliate> findByStatus(AffiliateStatus status, Pageable pageable);
}




