package com.hth.udecareer.repository;

import com.hth.udecareer.entities.LdQuizCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LdQuizCategoryRepository extends JpaRepository<LdQuizCategoryEntity, Long> {

    Optional<LdQuizCategoryEntity> findByCategoryName(String categoryName);

    List<LdQuizCategoryEntity> findAllByOrderByCategoryNameAsc();
}
