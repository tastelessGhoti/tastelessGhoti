package com.portfolio.ecommerce.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 값 객체 (Value Object)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    @Column(length = 10)
    private String zipCode;

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String detailAddress;
}
