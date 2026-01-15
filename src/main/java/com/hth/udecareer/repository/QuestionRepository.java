package com.hth.udecareer.repository;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.QuestionEntity;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    @Query("SELECT q.quizId, count(q.id) "
           + "FROM QuestionEntity q "
           + "WHERE q.online = 1 "
           + "GROUP BY q.quizId")
    List<Tuple> countQuestionGroupByQuizId();

    @Query("FROM QuestionEntity q "
           + "WHERE q.online = 1 "
           + "AND q.quizId = :quizId "
           + "ORDER BY q.sort")
    List<QuestionEntity> getQuestionByQuizId(@Param("quizId") Long quizId);

    List<QuestionEntity> findAllByIdIn(List<Long> questionIds);
}
