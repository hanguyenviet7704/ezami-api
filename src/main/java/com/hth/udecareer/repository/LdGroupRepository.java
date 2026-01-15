package com.hth.udecareer.repository;

import com.hth.udecareer.entities.LdGroup;
import com.hth.udecareer.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LdGroupRepository extends JpaRepository<LdGroup, Long> {

    List<LdGroup> findByStatusOrderByTitleAsc(PostStatus status);

    Optional<LdGroup> findByIdAndStatus(Long id, PostStatus status);

    Optional<LdGroup> findByName(String name);

    @Query("SELECT g FROM LdGroup g WHERE g.status = 'PUBLISH' ORDER BY g.title ASC")
    List<LdGroup> findAllPublished();

    Page<LdGroup> findByStatusOrderByDateDesc(PostStatus status, Pageable pageable);

    @Query("SELECT g FROM LdGroup g WHERE g.authorId = :authorId AND g.status = 'PUBLISH'")
    List<LdGroup> findByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT g FROM LdGroup g WHERE g.title LIKE %:keyword% AND g.status = 'PUBLISH'")
    List<LdGroup> searchByTitle(@Param("keyword") String keyword);
}
