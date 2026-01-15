package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserPurchasedEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for UserPurchasedEntity with JPA Specification support
 * Extends JpaSpecificationExecutor for dynamic query building
 */
@Repository
public interface UserPurchasedRepository extends JpaRepository<UserPurchasedEntity, Long>, 
                                                  JpaSpecificationExecutor<UserPurchasedEntity> {

    // Still used by other services
    List<UserPurchasedEntity> findAllByUserIdOrUserEmail(Long userId, String userEmail);
    
    List<UserPurchasedEntity> findByUserIdAndCategoryCode(Long userId, String categoryCode);
    
    boolean existsByUserIdAndCategoryCodeAndIsPurchased(Long userId, String categoryCode, Integer isPurchased);
    
    /**
     * Find all purchases by userId with QuizCategory eagerly fetched using EntityGraph
     * This prevents LazyInitializationException
     */
    @EntityGraph(attributePaths = {"quizCategory"})
    List<UserPurchasedEntity> findAllByUserId(Long userId);
}
