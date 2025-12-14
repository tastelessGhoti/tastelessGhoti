package com.kakaopay.account.domain.terms.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TermsAgreementRequest {

    @NotEmpty(message = "동의할 약관 목록은 필수입니다")
    private List<Long> termsIds;
}
