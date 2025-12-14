package com.kakaopay.account.domain.kyc.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KycVerificationType {

    ID_CARD("신분증 인증"),
    BANK_ACCOUNT("계좌 인증"),
    FACE_RECOGNITION("안면 인식"),
    DOCUMENT_UPLOAD("서류 제출");

    private final String description;
}
