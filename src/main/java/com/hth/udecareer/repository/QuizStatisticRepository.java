package com.hth.udecareer.repository;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.QuizStatisticEntity;
import com.hth.udecareer.entities.QuizStatisticId;

@Repository
public interface QuizStatisticRepository extends JpaRepository<QuizStatisticEntity, QuizStatisticId> {

    List<QuizStatisticEntity> findAllById_StatisticRefId(@NotNull Long statisticRefId);

    List<QuizStatisticEntity> findAllById_StatisticRefIdIn(@NotNull Collection<Long> statisticRefIds);
}
