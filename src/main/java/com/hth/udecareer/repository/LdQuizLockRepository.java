package com.hth.udecareer.repository;

import com.hth.udecareer.entities.LdQuizLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LdQuizLockRepository extends JpaRepository<LdQuizLockEntity, Long> {

    List<LdQuizLockEntity> findByQuizId(Long quizId);

    Optional<LdQuizLockEntity> findByQuizIdAndUserId(Long quizId, Long userId);

    Optional<LdQuizLockEntity> findByQuizIdAndLockIp(Long quizId, String lockIp);

    boolean existsByQuizIdAndUserId(Long quizId, Long userId);

    boolean existsByQuizIdAndLockIp(Long quizId, String lockIp);

    @Modifying
    @Query("DELETE FROM LdQuizLockEntity l WHERE l.lockDate < :expiryTime")
    int deleteExpiredLocks(@Param("expiryTime") Long expiryTime);

    void deleteByQuizIdAndUserId(Long quizId, Long userId);
}
