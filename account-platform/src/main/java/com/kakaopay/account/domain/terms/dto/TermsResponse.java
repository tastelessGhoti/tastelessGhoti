package com.kakaopay.account.domain.terms.dto;

import com.kakaopay.account.domain.terms.entity.Terms;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TermsResponse {

    private Long termsId;
    private String termsCode;
    private String title;
    private String content;
    private Integer version;
    private boolean required;
    private LocalDate effectiveDate;
    private boolean agreed;

    public static TermsResponse from(Terms terms) {
        return TermsResponse.builder()
                .termsId(terms.getId())
                .termsCode(terms.getTermsCode())
                .title(terms.getTitle())
                .content(terms.getContent())
                .version(terms.getVersion())
                .required(terms.isRequired())
                .effectiveDate(terms.getEffectiveDate())
                .agreed(false)
                .build();
    }

    public static TermsResponse from(Terms terms, boolean agreed) {
        return TermsResponse.builder()
                .termsId(terms.getId())
                .termsCode(terms.getTermsCode())
                .title(terms.getTitle())
                .content(terms.getContent())
                .version(terms.getVersion())
                .required(terms.isRequired())
                .effectiveDate(terms.getEffectiveDate())
                .agreed(agreed)
                .build();
    }
}
