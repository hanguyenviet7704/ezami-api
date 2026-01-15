package com.hth.udecareer.repository;

import com.hth.udecareer.entities.LdQuizFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LdQuizFormRepository extends JpaRepository<LdQuizFormEntity, Long> {

    List<LdQuizFormEntity> findByQuizIdOrderBySortAsc(Long quizId);

    List<LdQuizFormEntity> findByQuizIdAndRequiredOrderBySortAsc(Long quizId, Integer required);

    void deleteByQuizId(Long quizId);
}
