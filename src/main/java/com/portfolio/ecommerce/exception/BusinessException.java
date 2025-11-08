package com.portfolio.ecommerce.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외
 * ErrorCode를 사용하여 예외를 생성
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
