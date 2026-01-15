package com.hth.udecareer.repository;

import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

//
//    @Query("SELECT DISTINCT o FROM OrderEntity o " +
//            "LEFT JOIN FETCH o.items " +
//            "WHERE o.userId = :userId " +
//            "ORDER BY o.createdAt DESC")
//    List<OrderEntity> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
//
//
//    @Query(value = "SELECT o FROM OrderEntity o WHERE o.userId = :userId",
//           countQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.userId = :userId")
//    Page<OrderEntity> findAllByUserId(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "LEFT JOIN FETCH o.items " +
           "WHERE o.id IN :orderIds")
    List<OrderEntity> findAllWithItemsByIds(@Param("orderIds") List<Long> orderIds);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<OrderEntity> findWithItemsById(@Param("orderId") Long orderId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE OrderEntity o SET o.status = 'EXPIRED', o.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE o.status = 'PENDING' AND o.createdAt < :cutoffTime")
    int expirePendingOrdersBefore(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);

       @Query("SELECT o FROM OrderEntity o WHERE o.transactionNo = :txn")
       Optional<OrderEntity> findByTransactionNo(@Param("txn") String txn);

       @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.status = :status AND o.totalAmount = :amount ORDER BY o.createdAt DESC")
       Optional<OrderEntity> findFirstByStatusAndTotalAmountWithItems(@Param("status") OrderStatus status, @Param("amount") BigDecimal amount);


    @Query(value = "SELECT o FROM OrderEntity o WHERE o.userId = :userId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:fromDate IS NULL OR o.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR o.createdAt <= :toDate)",
           countQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.userId = :userId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:fromDate IS NULL OR o.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR o.createdAt <= :toDate)")
    Page<OrderEntity> findAllByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("status") com.hth.udecareer.enums.OrderStatus status,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            Pageable pageable);
}