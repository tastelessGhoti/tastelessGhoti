package com.portfolio.ecommerce.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 주문 리포지토리
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 사용자별 주문 조회 (페이징)
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * 사용자별 특정 상태의 주문 조회
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * 상태별 주문 조회 (페이징)
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * 사용자의 주문 건수 조회
     */
    long countByUserId(Long userId);

    /**
     * 주문 항목과 상품 정보를 함께 조회 (N+1 문제 방지)
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.product " +
           "WHERE o.id = :orderId")
    Order findByIdWithItems(@Param("orderId") Long orderId);
}
