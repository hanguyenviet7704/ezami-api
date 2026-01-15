package com.hth.udecareer.repository;

import com.hth.udecareer.entities.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.order.id = :orderId")
    List<OrderItemEntity> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.order.userId = :userId")
    List<OrderItemEntity> findByUserId(@Param("userId") Long userId);
}