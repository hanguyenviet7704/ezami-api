package com.hth.udecareer.repository;

import com.hth.udecareer.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
