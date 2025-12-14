package com.kakaopay.account.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역에서 사용하는 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러 (1000번대)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "지원하지 않는 HTTP 메서드입니다"),

    // 인증 에러 (2000번대)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 토큰입니다"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A003", "토큰이 존재하지 않습니다"),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "A004", "리프레시 토큰이 일치하지 않습니다"),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A005", "인증에 실패했습니다"),

    // 회원 에러 (3000번대)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다"),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "M002", "이미 가입된 회원입니다"),
    MEMBER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "M003", "이미 탈퇴한 회원입니다"),
    INVALID_MEMBER_STATUS(HttpStatus.BAD_REQUEST, "M004", "회원 상태가 올바르지 않습니다"),
    MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "M005", "이용 정지된 회원입니다"),

    // 약관 에러 (4000번대)
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "약관을 찾을 수 없습니다"),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "T002", "필수 약관에 동의해야 합니다"),
    TERMS_ALREADY_AGREED(HttpStatus.BAD_REQUEST, "T003", "이미 동의한 약관입니다"),
    TERMS_VERSION_MISMATCH(HttpStatus.CONFLICT, "T004", "약관 버전이 일치하지 않습니다"),

    // KYC 에러 (5000번대)
    KYC_NOT_FOUND(HttpStatus.NOT_FOUND, "K001", "KYC 정보를 찾을 수 없습니다"),
    KYC_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "K002", "이미 KYC 인증이 완료되었습니다"),
    KYC_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "K003", "KYC 인증에 실패했습니다"),
    KYC_DOCUMENT_EXPIRED(HttpStatus.BAD_REQUEST, "K004", "KYC 서류가 만료되었습니다"),
    KYC_REQUIRED(HttpStatus.FORBIDDEN, "K005", "KYC 인증이 필요합니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
