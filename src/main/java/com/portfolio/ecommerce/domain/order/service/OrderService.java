package com.portfolio.ecommerce.domain.order.service;

import com.portfolio.ecommerce.common.PageResponse;
import com.portfolio.ecommerce.domain.order.Order;
import com.portfolio.ecommerce.domain.order.OrderItem;
import com.portfolio.ecommerce.domain.order.OrderRepository;
import com.portfolio.ecommerce.domain.order.dto.OrderRequest;
import com.portfolio.ecommerce.domain.order.dto.OrderResponse;
import com.portfolio.ecommerce.domain.product.Product;
import com.portfolio.ecommerce.domain.product.ProductRepository;
import com.portfolio.ecommerce.domain.user.Address;
import com.portfolio.ecommerce.domain.user.User;
import com.portfolio.ecommerce.domain.user.UserRepository;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 서비스
 * 주문 생성, 조회, 취소 등 주문 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 배송지 설정 (요청에 있으면 사용, 없으면 사용자 기본 주소 사용)
        Address deliveryAddress = request.getAddress() != null ?
            new Address(request.getZipCode(), request.getAddress(), request.getDetailAddress()) :
            user.getAddress();

        // 주문 생성
        Order order = Order.builder()
            .user(user)
            .deliveryAddress(deliveryAddress)
            .orderMessage(request.getOrderMessage())
            .build();

        // 주문 항목 추가
        request.getOrderItems().forEach(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                    "상품을 찾을 수 없습니다. ID: " + itemRequest.getProductId()));

            OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(itemRequest.getQuantity())
                .build();

            order.addOrderItem(orderItem);
        });

        Order savedOrder = orderRepository.save(order);
        log.info("새로운 주문 생성: 사용자 {}, 주문 ID {}", userId, savedOrder.getId());

        return OrderResponse.from(savedOrder);
    }

    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                "주문을 찾을 수 없습니다. ID: " + orderId);
        }
        return OrderResponse.from(order);
    }

    /**
     * 사용자별 주문 목록 조회
     */
    public PageResponse<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        Page<OrderResponse> responses = orders.map(OrderResponse::from);
        return PageResponse.of(responses);
    }

    /**
     * 전체 주문 목록 조회 (관리자)
     */
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderResponse> responses = orders.map(OrderResponse::from);
        return PageResponse.of(responses);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 본인의 주문인지 확인
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                "본인의 주문만 취소할 수 있습니다.");
        }

        // 주문 취소
        order.cancel();
        log.info("주문 취소: 주문 ID {}", orderId);

        return OrderResponse.from(order);
    }

    /**
     * 주문 확인 (관리자)
     */
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.confirm();
        log.info("주문 확인: 주문 ID {}", orderId);

        return OrderResponse.from(order);
    }

    /**
     * 배송 시작 (관리자)
     */
    @Transactional
    public OrderResponse shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.ship();
        log.info("배송 시작: 주문 ID {}", orderId);

        return OrderResponse.from(order);
    }

    /**
     * 배송 완료 (관리자)
     */
    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.deliver();
        log.info("배송 완료: 주문 ID {}", orderId);

        return OrderResponse.from(order);
    }
}
