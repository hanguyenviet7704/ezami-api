package com.hth.udecareer.specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.model.request.PurchaseFilterRequest;

/**
 * JPA Specification builder for UserPurchasedEntity
 * Provides dynamic query building with JOIN to QuizCategoryEntity
 */
public class PurchaseSpecification {

    private PurchaseSpecification() {
        // Utility class - prevent instantiation
    }

    /**
     * Build dynamic specification for Purchase filtering
     *
     * @param filter Filter request containing all filter parameters
     * @return Specification for JPA query
     */
    public static Specification<UserPurchasedEntity> buildSpecification(PurchaseFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Required filter: userId (from JWT)
            addUserIdFilter(filter, predicates, criteriaBuilder, root);

            // Optional filters
            addKeywordFilter(filter, predicates, criteriaBuilder, root, query);
            addIsActiveFilter(filter, predicates, criteriaBuilder, root);
            addDateRangeFilter(filter, predicates, criteriaBuilder, root);

            // Avoid duplicate rows
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter by userId (REQUIRED)
     */
    private static void addUserIdFilter(PurchaseFilterRequest filter,
                                        List<Predicate> predicates,
                                        javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                        javax.persistence.criteria.Root<UserPurchasedEntity> root) {
        if (filter.getUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("userId"), filter.getUserId()));
        }
    }

    /**
     * Filter by keyword in QuizCategory title
     * Uses the JOIN already created in buildSpecification
     */
    private static void addKeywordFilter(PurchaseFilterRequest filter,
                                        List<Predicate> predicates,
                                        javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                        javax.persistence.criteria.Root<UserPurchasedEntity> root,
                                        javax.persistence.criteria.CriteriaQuery<?> query) {
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            // Use JOIN for filtering (not fetch, because fetch is already done above)
            Join<UserPurchasedEntity, QuizCategoryEntity> categoryJoin = 
                    root.join("quizCategory", JoinType.LEFT);

            String keyword = "%" + filter.getKeyword().trim().toLowerCase() + "%";
            
            Predicate titleMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(categoryJoin.get("title")), 
                    keyword
            );
            
            predicates.add(titleMatch);
        }
    }

    /**
     * Filter by isActive status
     * - true: còn hạn (to_time IS NULL OR to_time > NOW())
     * - false: hết hạn (to_time IS NOT NULL AND to_time <= NOW())
     * - null: tất cả
     */
    private static void addIsActiveFilter(PurchaseFilterRequest filter,
                                          List<Predicate> predicates,
                                          javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                          javax.persistence.criteria.Root<UserPurchasedEntity> root) {
        if (filter.getIsActive() != null) {
            LocalDateTime now = LocalDateTime.now();
            
            if (filter.getIsActive()) {
                // Còn hạn: to_time IS NULL OR to_time > NOW()
                Predicate noExpiry = criteriaBuilder.isNull(root.get("toTime"));
                Predicate notExpired = criteriaBuilder.greaterThan(root.get("toTime"), now);
                
                predicates.add(criteriaBuilder.or(noExpiry, notExpired));
            } else {
                // Hết hạn: to_time IS NOT NULL AND to_time <= NOW()
                Predicate hasExpiry = criteriaBuilder.isNotNull(root.get("toTime"));
                Predicate expired = criteriaBuilder.lessThanOrEqualTo(root.get("toTime"), now);
                
                predicates.add(criteriaBuilder.and(hasExpiry, expired));
            }
        }
    }

    /**
     * Filter by purchase date range (from_time)
     */
    private static void addDateRangeFilter(PurchaseFilterRequest filter,
                                          List<Predicate> predicates,
                                          javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                          javax.persistence.criteria.Root<UserPurchasedEntity> root) {
        if (filter.getFromDate() != null) {
            LocalDateTime startOfDay = LocalDateTime.of(filter.getFromDate(), LocalTime.MIN);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fromTime"), startOfDay));
        }

        if (filter.getToDate() != null) {
            LocalDateTime endOfDay = LocalDateTime.of(filter.getToDate(), LocalTime.MAX);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fromTime"), endOfDay));
        }
    }
}
