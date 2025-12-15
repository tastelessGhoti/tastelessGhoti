package com.paygate.payment.common.exception;

import lombok.Getter;

/**
 * VAN사 연동 중 발생하는 예외.
 * 외부 시스템과의 통신 오류를 표현.
 */
@Getter
public class VanException extends BusinessException {

    private final String vanCode;
    private final String vanMessage;

    public VanException(ErrorCode errorCode, String vanCode, String vanMessage) {
        super(errorCode, String.format("VAN 응답코드: %s, 메시지: %s", vanCode, vanMessage));
        this.vanCode = vanCode;
        this.vanMessage = vanMessage;
    }

    public VanException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
        this.vanCode = null;
        this.vanMessage = null;
    }
}
