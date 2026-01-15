package com.hth.udecareer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hth.udecareer.entities.QuizMaster;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.model.dto.QuizDto;

@Repository
public interface QuizMasterRepository extends JpaRepository<QuizMaster, Long> {

    @Query("SELECT qm.id AS id, p.name AS slug,  qm.timeLimit AS timeLimit, qm.name AS name, p.id AS postId,"
           + " p.content AS postContent, p.title AS postTitle, p.status as postStatus "
           + "FROM QuizMaster qm, PostMeta pm, Post p "
           + "WHERE qm.id = CAST(pm.metaValue AS integer ) "
           + "AND pm.postId = p.id "
           + "AND pm.metaKey = 'quiz_pro_id' "
           + "AND p.status in :postStatuses "
           + "AND qm.id = :quizId")
    Optional<QuizDto> findActiveQuizById(@Param("quizId") Long quizId,
                                         List<PostStatus> postStatuses);

    @Query("SELECT qm.id AS id, p.name AS slug,  qm.timeLimit AS timeLimit, qm.name AS name, p.id AS postId,"
           + " p.content AS postContent, p.title AS postTitle "
           + "FROM QuizMaster qm, PostMeta pm, Post p "
           + "WHERE qm.id = CAST(pm.metaValue AS integer ) "
           + "AND pm.postId = p.id "
           + "AND p.status in :postStatuses "
           + "AND p.type = :postType "
           + "AND pm.metaKey = 'quiz_pro_id'")
    List<QuizDto> findActiveQuizzes(List<PostStatus> postStatuses, String postType);

    List<QuizMaster> findAllByIdIn(List<Long> ids);

    @Query(value = "SELECT qm.id AS id, p.name AS slug, qm.timeLimit AS timeLimit, qm.name AS name, p.id AS postId, "
            + " p.content AS postContent, p.title AS postTitle "
            + "FROM QuizMaster qm "
            + "INNER JOIN PostMeta pm ON qm.id = CAST(pm.metaValue AS integer) "
            + "INNER JOIN Post p ON pm.postId = p.id "
            + "LEFT JOIN PostMeta pm_course ON p.id = pm_course.postId AND pm_course.metaKey = 'course_id' "
            + "WHERE pm.metaKey = 'quiz_pro_id' "
            + "AND p.status IN :postStatuses "
            + "AND p.type = :postType "
            + "AND (:courseIdValue IS NULL OR pm_course.metaValue = :courseIdValue)"
            + "AND (:useCategory = false OR LOWER(qm.name) LIKE :categoryLike) "
            + "AND (:onlyMini = false OR LOWER(qm.name) LIKE :miniLike) "
            + "AND (:excludeMini = false OR LOWER(qm.name) NOT LIKE :miniLike) "
            + "AND (:useMinTimeLimit = false OR qm.timeLimit >= :minTimeLimit) "
            + "AND (:useMaxTimeLimit = false OR qm.timeLimit <= :maxTimeLimit)",
            countQuery = "SELECT COUNT(qm.id) "
            + "FROM QuizMaster qm "
            + "INNER JOIN PostMeta pm ON qm.id = CAST(pm.metaValue AS integer) "
            + "INNER JOIN Post p ON pm.postId = p.id "
            + "LEFT JOIN PostMeta pm_course ON p.id = pm_course.postId AND pm_course.metaKey = 'course_id' "
            + "WHERE pm.metaKey = 'quiz_pro_id' "
            + "AND p.status IN :postStatuses "
            + "AND p.type = :postType "
            + "AND (:courseIdValue IS NULL OR pm_course.metaValue = :courseIdValue)"
            + "AND (:useCategory = false OR LOWER(qm.name) LIKE :categoryLike) "
            + "AND (:onlyMini = false OR LOWER(qm.name) LIKE :miniLike) "
            + "AND (:excludeMini = false OR LOWER(qm.name) NOT LIKE :miniLike) "
            + "AND (:useMinTimeLimit = false OR qm.timeLimit >= :minTimeLimit) "
            + "AND (:useMaxTimeLimit = false OR qm.timeLimit <= :maxTimeLimit)")
    Page<QuizDto> findActiveQuizzesPage(@Param("postStatuses") List<PostStatus> postStatuses,
                                        @Param("postType") String postType,
                                        @Param("useCategory") boolean useCategory,
                                        @Param("categoryLike") String categoryLike,
                                        @Param("onlyMini") boolean onlyMini,
                                        @Param("excludeMini") boolean excludeMini,
                                        @Param("miniLike") String miniLike,
                                        @Param("useMinTimeLimit") boolean useMinTimeLimit,
                                        @Param("minTimeLimit") Integer minTimeLimit,
                                        @Param("useMaxTimeLimit") boolean useMaxTimeLimit,
                                        @Param("maxTimeLimit") Integer maxTimeLimit,
                                        @Param("courseIdValue") String courseIdValue,
                                        Pageable pageable);
}
