package com.hth.udecareer.repository;

import com.hth.udecareer.entities.AppLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppLogRepository extends JpaRepository<AppLogEntity, Long> {
}
