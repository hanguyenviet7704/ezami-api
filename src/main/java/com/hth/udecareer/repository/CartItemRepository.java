package com.hth.udecareer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hth.udecareer.entities.CartItemEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    boolean existsByUserIdAndCategoryCodeAndPlanCode(Long userId, String categoryCode, String planCode);

    void deleteByUserIdAndId(Long userId, Long id);
}
