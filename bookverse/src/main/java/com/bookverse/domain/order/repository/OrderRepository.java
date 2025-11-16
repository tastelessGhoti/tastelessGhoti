package com.bookverse.domain.order.repository;

import com.bookverse.domain.order.entity.Order;
import com.bookverse.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 주문 Repository
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 사용자별 주문 조회
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자별 주문 상태별 조회
     */
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
}
