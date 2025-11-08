package com.portfolio.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 커스텀 예외 클래스
 * 비즈니스 로직에서 발생하는 모든 예외의 기본 클래스
 */
@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public CustomException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public CustomException(String message, HttpStatus status) {
        this(message, status, status.name());
    }
}
