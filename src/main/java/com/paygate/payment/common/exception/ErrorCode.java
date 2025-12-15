package com.paygate.payment.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 결제 시스템 에러 코드 정의.
 * 접두사로 도메인을 구분하여 에러 추적이 용이하도록 설계.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러 (C)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 오류가 발생했습니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C003", "잘못된 타입입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "허용되지 않은 메서드입니다"),

    // 결제 에러 (P)
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제 정보를 찾을 수 없습니다"),
    PAYMENT_ALREADY_APPROVED(HttpStatus.CONFLICT, "P002", "이미 승인된 결제입니다"),
    PAYMENT_ALREADY_CANCELED(HttpStatus.CONFLICT, "P003", "이미 취소된 결제입니다"),
    PAYMENT_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "P004", "취소 금액이 결제 금액을 초과합니다"),
    PAYMENT_APPROVAL_FAILED(HttpStatus.BAD_GATEWAY, "P005", "결제 승인에 실패했습니다"),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "P006", "결제 취소에 실패했습니다"),
    PAYMENT_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "P007", "결제 처리 시간이 초과되었습니다"),
    PAYMENT_PROCESSING(HttpStatus.CONFLICT, "P008", "결제가 처리 중입니다"),
    INVALID_CARD_INFO(HttpStatus.BAD_REQUEST, "P009", "유효하지 않은 카드 정보입니다"),
    DUPLICATE_ORDER_ID(HttpStatus.CONFLICT, "P010", "중복된 주문번호입니다"),

    // 가맹점 에러 (M)
    MERCHANT_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "가맹점 정보를 찾을 수 없습니다"),
    MERCHANT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "M002", "비활성화된 가맹점입니다"),
    INVALID_API_KEY(HttpStatus.UNAUTHORIZED, "M003", "유효하지 않은 API 키입니다"),

    // VAN 에러 (V)
    VAN_CONNECTION_ERROR(HttpStatus.BAD_GATEWAY, "V001", "VAN사 연결에 실패했습니다"),
    VAN_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "V002", "VAN사 응답 시간이 초과되었습니다"),
    VAN_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, "V003", "VAN사 응답 처리 중 오류가 발생했습니다"),

    // 동시성 에러 (L)
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "L001", "락 획득에 실패했습니다. 잠시 후 다시 시도해주세요"),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "L002", "동시 수정이 감지되었습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
