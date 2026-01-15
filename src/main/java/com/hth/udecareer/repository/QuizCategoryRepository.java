package com.hth.udecareer.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hth.udecareer.entities.QuizCategoryEntity;

@Repository
public interface QuizCategoryRepository extends JpaRepository<QuizCategoryEntity, Long> {

    @Query("select qc from QuizCategoryEntity qc where qc.enable = true order by qc.order")
    List<QuizCategoryEntity> findAllActiveOrderByOrder();

    @Query("select qc from QuizCategoryEntity qc where qc.enable = true order by qc.order")
    Page<QuizCategoryEntity> findAllActiveOrderByOrder(Pageable pageable);

    @Query("select qc from QuizCategoryEntity qc where qc.enable = true and " +
           "(lower(qc.title) like lower(concat('%', :title, '%')) or lower(qc.code) like lower(concat('%', :title, '%')))")
    Page<QuizCategoryEntity> findAllActiveOrderByOrderWithTitleFilter(String title, Pageable pageable);

    @Query("select qc from QuizCategoryEntity qc where qc.enable = true and " +
           "(lower(qc.title) like lower(concat('%', :title, '%')) or lower(qc.code) like lower(concat('%', :title, '%'))) order by qc.order")
    List<QuizCategoryEntity> findAllActiveOrderByOrderWithTitleFilter(String title);

    boolean existsByTitle(String title);

    boolean existsByCode(String code);

    Optional<QuizCategoryEntity> findByCode(String code);

    // Batch fetch by codes to avoid N+1 queries
    List<QuizCategoryEntity> findByCodeIn(Collection<String> codes);

    @Query("""
        SELECT qc, COUNT(DISTINCT q.id)
        FROM QuizCategoryEntity qc
        LEFT JOIN LdQuizCategoryEntity ldc ON ldc.categoryName = qc.code
        LEFT JOIN QuestionEntity q ON q.categoryId = ldc.id AND q.online = 1
        WHERE qc.enable = true
        GROUP BY qc.id, qc.code, qc.title, qc.header, qc.imageUri, qc.order, qc.enable
        ORDER BY qc.order
    """)
    List<Object[]> findAllActiveWithQuizCount();
}
