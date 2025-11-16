package com.bookverse.domain.order.service;

import com.bookverse.domain.book.entity.Book;
import com.bookverse.domain.book.repository.BookRepository;
import com.bookverse.domain.order.dto.OrderCreateRequest;
import com.bookverse.domain.order.dto.OrderResponse;
import com.bookverse.domain.order.entity.Order;
import com.bookverse.domain.order.entity.OrderItem;
import com.bookverse.domain.order.entity.OrderStatus;
import com.bookverse.domain.order.repository.OrderRepository;
import com.bookverse.domain.user.entity.User;
import com.bookverse.domain.user.repository.UserRepository;
import com.bookverse.exception.BusinessException;
import com.bookverse.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 비즈니스 로직 서비스
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public Long createOrder(String userEmail, OrderCreateRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 주문 항목 생성
        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> {
                    Book book = bookRepository.findById(item.getBookId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
                    return OrderItem.createOrderItem(book, item.getQuantity());
                })
                .collect(Collectors.toList());

        // 주문 생성
        Order order = Order.createOrder(user, request.getDeliveryAddress(),
                request.getOrderMessage(), orderItems);

        // 결제 정보 설정 (실제로는 PG사 연동)
        order.setPaymentInfo(request.getPaymentMethod(), "PAYMENT_KEY_" + System.currentTimeMillis());
        order.updateStatus(OrderStatus.PAID);

        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성: 사용자={}, 주문ID={}, 총액={}원", user.getEmail(), savedOrder.getId(), savedOrder.getTotalPrice());

        return savedOrder.getId();
    }

    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문만 조회 가능
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return OrderResponse.from(order);
    }

    /**
     * 내 주문 목록 조회
     */
    public Page<OrderResponse> getMyOrders(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);
        return orders.map(OrderResponse::from);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문만 취소 가능
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        order.cancel();
        log.info("주문 취소: 주문ID={}", orderId);
    }
}
