package com.hth.udecareer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.VerificationCodeEntity;
import com.hth.udecareer.enums.VerificationCodeType;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    Optional<VerificationCodeEntity> findByEmailAndCodeAndType(String email,
                                                               String code,
                                                               VerificationCodeType type);

    List<VerificationCodeEntity> findByEmailAndType(String email,
                                                    VerificationCodeType type);
}
