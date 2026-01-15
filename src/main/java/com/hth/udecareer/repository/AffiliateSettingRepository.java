package com.hth.udecareer.repository;

import com.hth.udecareer.entities.AffiliateSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AffiliateSettingRepository extends JpaRepository<AffiliateSetting, Long> {
    Optional<AffiliateSetting> findBySettingKey(String key);
}




