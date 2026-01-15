package com.hth.udecareer.repository;

import com.hth.udecareer.entities.LdQuizPrerequisiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LdQuizPrerequisiteRepository extends JpaRepository<LdQuizPrerequisiteEntity, Long> {

    List<LdQuizPrerequisiteEntity> findByQuizId(Long quizId);

    @Query("SELECT p.prerequisiteQuizId FROM LdQuizPrerequisiteEntity p WHERE p.quizId = :quizId")
    List<Long> findPrerequisiteQuizIds(@Param("quizId") Long quizId);

    @Query("SELECT p.quizId FROM LdQuizPrerequisiteEntity p WHERE p.prerequisiteQuizId = :prerequisiteQuizId")
    List<Long> findQuizesRequiringPrerequisite(@Param("prerequisiteQuizId") Long prerequisiteQuizId);

    boolean existsByQuizIdAndPrerequisiteQuizId(Long quizId, Long prerequisiteQuizId);

    void deleteByQuizId(Long quizId);
}
