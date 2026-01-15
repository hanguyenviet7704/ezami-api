package com.hth.udecareer.repository;

import com.hth.udecareer.entities.SupportLog;
import com.hth.udecareer.model.response.SupportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportLogRepository extends JpaRepository<SupportLog, Long> {

    Page<SupportLog> findByUserId(Long userId, Pageable pageable);

}
