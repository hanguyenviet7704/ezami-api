package com.hth.udecareer.repository;

import com.hth.udecareer.entities.LdQuizTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LdQuizTemplateRepository extends JpaRepository<LdQuizTemplateEntity, Long> {

    Optional<LdQuizTemplateEntity> findByName(String name);

    List<LdQuizTemplateEntity> findByType(Integer type);

    List<LdQuizTemplateEntity> findAllByOrderByNameAsc();
}
