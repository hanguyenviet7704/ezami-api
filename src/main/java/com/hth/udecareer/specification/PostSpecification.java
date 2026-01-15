package com.hth.udecareer.specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.hth.udecareer.entities.Post;
import com.hth.udecareer.entities.TermEntity;
import com.hth.udecareer.entities.TermRelationshipEntity;
import com.hth.udecareer.entities.TermTaxonomyEntity;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.model.request.PostFilterRequest;

/**
 * JPA Specification builder for Post entity
 * Provides dynamic query building with optimized EXISTS subqueries
 */
public class PostSpecification {

    private static final String POST_TYPE = "post";
    private static final String TAXONOMY_CATEGORY = "category";
    private static final String TAXONOMY_TAG = "post_tag";

    private PostSpecification() {
        // Utility class - prevent instantiation
    }

    /**
     * Build dynamic specification for Post filtering
     * Optimized with EXISTS subqueries instead of IN for better performance
     *
     * @param filter Filter request containing all filter parameters
     * @return Specification for JPA query
     */
    public static Specification<Post> buildSpecification(PostFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base filters
            addTypeFilter(predicates, criteriaBuilder, root);
            addStatusFilter(predicates, root);

            // Optional filters
            addKeywordFilter(filter, predicates, criteriaBuilder, root);
            addCategoryFilter(filter, predicates, criteriaBuilder, root, query);
            addAuthorFilter(filter, predicates, criteriaBuilder, root);
            addTagsFilter(filter, predicates, criteriaBuilder, root, query);
            addDateRangeFilter(filter, predicates, criteriaBuilder, root);

            // Avoid duplicate rows
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addTypeFilter(List<Predicate> predicates, 
                                      javax.persistence.criteria.CriteriaBuilder criteriaBuilder, 
                                      Root<Post> root) {
        predicates.add(criteriaBuilder.equal(root.get("type"), POST_TYPE));
    }

    private static void addStatusFilter(List<Predicate> predicates, Root<Post> root) {
        predicates.add(root.get("status").in(
                PostStatus.PUBLISH,
                PostStatus.FUTURE,
                PostStatus.PRIVATE
        ));
    }

    private static void addKeywordFilter(PostFilterRequest filter,
                                        List<Predicate> predicates,
                                        javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                        Root<Post> root) {
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            String keyword = "%" + filter.getKeyword().trim().toLowerCase() + "%";
            
            Predicate titleMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    keyword
            );
            Predicate contentMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("content")), 
                    keyword
            );
            
            predicates.add(criteriaBuilder.or(titleMatch, contentMatch));
        }
    }

    private static void addCategoryFilter(PostFilterRequest filter,
                                         List<Predicate> predicates,
                                         javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                         Root<Post> root,
                                         javax.persistence.criteria.CriteriaQuery<?> query) {
        if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
            Subquery<Long> categorySubquery = query.subquery(Long.class);
            Root<TermRelationshipEntity> trRoot = categorySubquery.from(TermRelationshipEntity.class);
            Join<TermRelationshipEntity, TermTaxonomyEntity> ttJoin = 
                    trRoot.join("termTaxonomy", JoinType.INNER);

            // Use term_taxonomy_id (categoryIds = term_taxonomy_id in WordPress)
            categorySubquery.select(criteriaBuilder.literal(1L))
                    .where(
                            criteriaBuilder.equal(trRoot.get("id").get("objectId"), root.get("id")),
                            criteriaBuilder.equal(ttJoin.get("taxonomy"), TAXONOMY_CATEGORY),
                            ttJoin.get("id").in(filter.getCategoryIds())
                    );

            predicates.add(criteriaBuilder.exists(categorySubquery));
        }
    }

    private static void addAuthorFilter(PostFilterRequest filter,
                                       List<Predicate> predicates,
                                       javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                       Root<Post> root) {
        if (filter.getAuthorId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("author"), filter.getAuthorId()));
        }
    }

    private static void addTagsFilter(PostFilterRequest filter,
                                     List<Predicate> predicates,
                                     javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                     Root<Post> root,
                                     javax.persistence.criteria.CriteriaQuery<?> query) {
        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            Subquery<Long> tagSubquery = query.subquery(Long.class);
            Root<TermRelationshipEntity> trRoot = tagSubquery.from(TermRelationshipEntity.class);
            Join<TermRelationshipEntity, TermTaxonomyEntity> ttJoin = 
                    trRoot.join("termTaxonomy", JoinType.INNER);
            Join<TermTaxonomyEntity, TermEntity> termJoin = 
                    ttJoin.join("term", JoinType.INNER);

            tagSubquery.select(criteriaBuilder.literal(1L))
                    .where(
                            criteriaBuilder.equal(trRoot.get("id").get("objectId"), root.get("id")),
                            criteriaBuilder.equal(ttJoin.get("taxonomy"), TAXONOMY_TAG),
                            termJoin.get("slug").in(filter.getTags())
                    );

            predicates.add(criteriaBuilder.exists(tagSubquery));
        }
    }

    private static void addDateRangeFilter(PostFilterRequest filter,
                                          List<Predicate> predicates,
                                          javax.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                          Root<Post> root) {
        if (filter.getFromDate() != null) {
            LocalDateTime startOfDay = LocalDateTime.of(filter.getFromDate(), LocalTime.MIN);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startOfDay));
        }

        if (filter.getToDate() != null) {
            LocalDateTime endOfDay = LocalDateTime.of(filter.getToDate(), LocalTime.MAX);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endOfDay));
        }
    }
}
