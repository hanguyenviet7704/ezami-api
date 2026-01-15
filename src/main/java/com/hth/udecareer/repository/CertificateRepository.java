package com.hth.udecareer.repository;

import com.hth.udecareer.entities.Certificate;
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
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByStatusOrderByTitleAsc(PostStatus status);

    Optional<Certificate> findByIdAndStatus(Long id, PostStatus status);

    Optional<Certificate> findByName(String name);

    @Query("SELECT c FROM Certificate c WHERE c.status = 'PUBLISH' ORDER BY c.title ASC")
    List<Certificate> findAllPublished();

    Page<Certificate> findByStatusOrderByDateDesc(PostStatus status, Pageable pageable);

    @Query("SELECT c FROM Certificate c WHERE c.title LIKE %:keyword% AND c.status = 'PUBLISH'")
    List<Certificate> searchByTitle(@Param("keyword") String keyword);
}
