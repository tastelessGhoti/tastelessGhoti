package com.kakaopay.account.domain.terms.entity;

import com.kakaopay.account.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "terms", indexes = {
        @Index(name = "idx_terms_code_version", columnList = "terms_code, version", unique = true),
        @Index(name = "idx_terms_active", columnList = "is_active, effective_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terms_code", nullable = false, length = 50)
    private String termsCode;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder
    private Terms(String termsCode, String title, String content, Integer version,
                  boolean required, LocalDate effectiveDate, Integer displayOrder) {
        this.termsCode = termsCode;
        this.title = title;
        this.content = content;
        this.version = version;
        this.required = required;
        this.active = true;
        this.effectiveDate = effectiveDate;
        this.displayOrder = displayOrder;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isEffective() {
        return this.active && !LocalDate.now().isBefore(this.effectiveDate);
    }
}
