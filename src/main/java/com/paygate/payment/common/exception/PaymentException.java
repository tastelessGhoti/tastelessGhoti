package com.paygate.payment.common.exception;

/**
 * 결제 처리 중 발생하는 예외.
 */
public class PaymentException extends BusinessException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public PaymentException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
