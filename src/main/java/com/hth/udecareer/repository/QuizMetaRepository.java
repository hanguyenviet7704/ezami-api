package com.hth.udecareer.repository;

import com.hth.udecareer.entities.QuizMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuizMetaRepository extends JpaRepository<QuizMetaEntity, Long> {
    List<QuizMetaEntity> findAllByQuestionTitleIn(Collection<String> questionTitles);
}

