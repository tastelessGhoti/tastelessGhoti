package com.paygate.payment.infrastructure.van;

/**
 * VAN사 통신 클라이언트 인터페이스.
 * 다양한 VAN사(NICE, KIS 등)를 동일한 방식으로 사용하기 위한 추상화.
 */
public interface VanClient {

    /**
     * 결제 승인 요청.
     */
    VanApprovalResponse approve(VanApprovalRequest request);

    /**
     * 결제 취소 요청.
     */
    VanCancelResponse cancel(VanCancelRequest request);

    /**
     * VAN사 식별자.
     */
    String getVanType();
}
