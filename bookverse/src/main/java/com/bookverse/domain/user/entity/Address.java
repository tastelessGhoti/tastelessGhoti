package com.bookverse.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * 주소 값 객체 (Value Object)
 * 재사용 가능한 주소 정보
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address {

    @Column(length = 10)
    private String zipCode;         // 우편번호

    @Column(length = 200)
    private String address;         // 기본 주소

    @Column(length = 200)
    private String detailAddress;   // 상세 주소
}
