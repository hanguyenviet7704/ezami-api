package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourseEntity, Long> {

    Optional<UserCourseEntity> findByUserIdAndCourseId(Long userId, Long courseId);

}
