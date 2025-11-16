package com.bookverse.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 처리 중 발생하는 예외
 * 이 예외는 예상 가능한 비즈니스 규칙 위반 시 발생
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
