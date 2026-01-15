package com.hth.udecareer.repository;

import com.hth.udecareer.entities.LdQuizToplistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LdQuizToplistRepository extends JpaRepository<LdQuizToplistEntity, Long> {

    List<LdQuizToplistEntity> findByQuizIdOrderByPointsDesc(Long quizId);

    Page<LdQuizToplistEntity> findByQuizIdOrderByPointsDesc(Long quizId, Pageable pageable);

    List<LdQuizToplistEntity> findByUserId(Long userId);

    Page<LdQuizToplistEntity> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    @Query("SELECT t FROM LdQuizToplistEntity t WHERE t.quizId = :quizId ORDER BY t.points DESC, t.result DESC")
    List<LdQuizToplistEntity> findTopByQuizId(@Param("quizId") Long quizId, Pageable pageable);

    @Query("SELECT t FROM LdQuizToplistEntity t ORDER BY t.points DESC, t.result DESC")
    List<LdQuizToplistEntity> findGlobalTop(Pageable pageable);

    @Query("SELECT COUNT(t) FROM LdQuizToplistEntity t WHERE t.quizId = :quizId AND t.points > :points")
    long countBetterScores(@Param("quizId") Long quizId, @Param("points") Integer points);

    @Query("SELECT MAX(t.points) FROM LdQuizToplistEntity t WHERE t.quizId = :quizId AND t.userId = :userId")
    Integer findBestScoreByQuizAndUser(@Param("quizId") Long quizId, @Param("userId") Long userId);

    void deleteByQuizIdAndUserId(Long quizId, Long userId);
}
