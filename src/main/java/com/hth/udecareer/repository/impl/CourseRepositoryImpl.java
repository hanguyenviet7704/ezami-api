package com.hth.udecareer.repository.impl;

import com.hth.udecareer.entities.Course;
import com.hth.udecareer.repository.custom.CourseRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

@Slf4j
public class CourseRepositoryImpl implements CourseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private boolean checkLike(String key) { // dung de check cac truong ma check theo LIKE
        String[] keyList = {"post_title"};
        for (String s : keyList) {
            if (s.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCategory(String key) {
        return "categoryId".equals(key);
    }

    @Override
    public List<Course> findAllCoursesByConditions(Map<String, Object> conditions) {
        // impl sql native
        StringBuilder sql =
                new StringBuilder(" SELECT wpp.* FROM wp_posts wpp WHERE 1=1 AND wpp.post_type = :post_type ");

        for (Map.Entry<String, Object> ets : conditions.entrySet()) {
            if (checkLike(ets.getKey())) { // LIKE %...%
                sql.append(" AND wpp.").append(ets.getKey()).append(" LIKE :").append(ets.getKey()).append(" ");
            } else {
                sql.append(" AND wpp.").append(ets.getKey()).append(" = :").append(ets.getKey()).append(" ");
            }
        }
        log.info(sql.toString());
        Query query = entityManager.createNativeQuery(sql.toString(), Course.class);
        query.setParameter("post_type", "sfwd-courses");

        // set parameter
        for (Map.Entry<String, Object> ets : conditions.entrySet()) {
            if (checkLike(ets.getKey())) {
                query.setParameter(ets.getKey(), "%" + ets.getValue().toString() + "%");
            } else {
                query.setParameter(ets.getKey(), ets.getValue());
            }
        }
        log.info(query.toString());
        return query.getResultList();
    }

    @Override
    public Page<Course> findAllCoursesByConditions(Map<String, Object> conditions, Pageable pageable) {
        // data query
        StringBuilder dataSql =
                new StringBuilder(" SELECT wpp.* FROM wp_posts wpp ");

        boolean hasCategory = conditions.containsKey("categoryId");

        if (hasCategory) {
            dataSql.append(" JOIN wp_term_relationships wtr ON wtr.object_id = wpp.ID ");
        }

        dataSql.append(" WHERE 1=1 AND wpp.post_type = :post_type AND wpp.post_status = :post_status ");

        for (Map.Entry<String, Object> ets : conditions.entrySet()) {
            if (checkLike(ets.getKey())) {
                dataSql.append(" AND wpp.").append(ets.getKey()).append(" LIKE :").append(ets.getKey()).append(" ");
            }  else if (checkCategory(ets.getKey())) {
                dataSql.append(" AND wtr.term_taxonomy_id = :categoryId ");
            } else {
                dataSql.append(" AND wpp.").append(ets.getKey()).append(" = :").append(ets.getKey()).append(" ");
            }
        }

        // count query (same WHERE)
        StringBuilder countSql =
                new StringBuilder(" SELECT COUNT(1) FROM wp_posts wpp ");

        if (hasCategory) {
            countSql.append(" JOIN wp_term_relationships wtr ON wtr.object_id = wpp.ID ");
        }

        countSql.append(" WHERE 1=1 AND wpp.post_type = :post_type AND wpp.post_status = :post_status ");

        for (Map.Entry<String, Object> ets : conditions.entrySet()) {
            if (checkLike(ets.getKey())) {
                countSql.append(" AND wpp.").append(ets.getKey()).append(" LIKE :").append(ets.getKey()).append(" ");
            } else if (checkCategory(ets.getKey())) {
                countSql.append(" AND wtr.term_taxonomy_id = :categoryId ");
            } else {
                countSql.append(" AND wpp.").append(ets.getKey()).append(" = :").append(ets.getKey()).append(" ");
            }
        }

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            dataSql.append(" ORDER BY wpp.").append(order.getProperty()).append(" ").append(order.getDirection().name());
        }

        // build data query
        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), Course.class);
        dataQuery.setParameter("post_type", "sfwd-courses");
        dataQuery.setParameter("post_status", "publish");

        // build count query
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        countQuery.setParameter("post_type", "sfwd-courses");
        countQuery.setParameter("post_status", "publish");

        // set parameters
        for (Map.Entry<String, Object> ets : conditions.entrySet()) {
            Object value = ets.getValue();
            if (checkLike(ets.getKey())) {
                value = "%" + value.toString() + "%";
            }
            dataQuery.setParameter(ets.getKey(), value);
            countQuery.setParameter(ets.getKey(), value);
        }

        // pagination
        int firstResult = (int) pageable.getOffset();
        dataQuery.setFirstResult(firstResult);
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Course> content = dataQuery.getResultList();
        Number total = (Number) countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total.longValue());
    }
}
