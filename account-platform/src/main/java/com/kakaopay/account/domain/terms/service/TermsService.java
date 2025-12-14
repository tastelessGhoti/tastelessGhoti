package com.kakaopay.account.domain.terms.service;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.domain.terms.dto.TermsAgreementRequest;
import com.kakaopay.account.domain.terms.dto.TermsResponse;
import com.kakaopay.account.domain.terms.entity.Terms;
import com.kakaopay.account.domain.terms.entity.TermsAgreement;
import com.kakaopay.account.domain.terms.repository.TermsAgreementRepository;
import com.kakaopay.account.domain.terms.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;
    private final TermsAgreementRepository termsAgreementRepository;

    @Transactional(readOnly = true)
    public List<TermsResponse> getAllActiveTerms() {
        return termsRepository.findAllActiveTerms(LocalDate.now()).stream()
                .map(TermsResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TermsResponse> getMemberTermsWithAgreementStatus(Long memberId) {
        List<Terms> activeTerms = termsRepository.findAllActiveTerms(LocalDate.now());
        Set<Long> agreedTermsIds = new HashSet<>(termsAgreementRepository.findAgreedTermsIds(memberId));

        return activeTerms.stream()
                .map(terms -> TermsResponse.from(terms, agreedTermsIds.contains(terms.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TermsResponse getTerms(Long termsId) {
        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TERMS_NOT_FOUND));
        return TermsResponse.from(terms);
    }

    @Transactional
    public void agreeToTerms(Long memberId, TermsAgreementRequest request, String ipAddress) {
        List<Terms> requiredTerms = termsRepository.findAllRequiredTerms(LocalDate.now());
        Set<Long> requiredTermsIds = requiredTerms.stream()
                .map(Terms::getId)
                .collect(Collectors.toSet());

        Set<Long> requestedIds = new HashSet<>(request.getTermsIds());

        if (!requestedIds.containsAll(requiredTermsIds)) {
            throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        for (Long termsId : request.getTermsIds()) {
            Terms terms = termsRepository.findById(termsId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TERMS_NOT_FOUND));

            if (!terms.isEffective()) {
                throw new BusinessException(ErrorCode.TERMS_NOT_FOUND, "유효하지 않은 약관입니다: " + termsId);
            }

            boolean alreadyAgreed = termsAgreementRepository
                    .existsByMemberIdAndTermsIdAndWithdrawnAtIsNull(memberId, termsId);

            if (alreadyAgreed) {
                log.debug("이미 동의한 약관 스킵: memberId={}, termsId={}", memberId, termsId);
                continue;
            }

            TermsAgreement agreement = TermsAgreement.builder()
                    .memberId(memberId)
                    .termsId(termsId)
                    .agreedIp(ipAddress)
                    .build();

            termsAgreementRepository.save(agreement);
        }

        log.info("약관 동의 완료: memberId={}, termsIds={}", memberId, request.getTermsIds());
    }

    @Transactional
    public void withdrawTermsAgreement(Long memberId, Long termsId) {
        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TERMS_NOT_FOUND));

        if (terms.isRequired()) {
            throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED,
                    "필수 약관은 철회할 수 없습니다");
        }

        TermsAgreement agreement = termsAgreementRepository.findActiveAgreement(memberId, termsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TERMS_NOT_FOUND,
                        "동의 내역을 찾을 수 없습니다"));

        agreement.withdraw();
        log.info("약관 동의 철회: memberId={}, termsId={}", memberId, termsId);
    }

    @Transactional(readOnly = true)
    public boolean hasAgreedToAllRequiredTerms(Long memberId) {
        List<Terms> requiredTerms = termsRepository.findAllRequiredTerms(LocalDate.now());
        Set<Long> agreedTermsIds = new HashSet<>(termsAgreementRepository.findAgreedTermsIds(memberId));

        return requiredTerms.stream()
                .allMatch(terms -> agreedTermsIds.contains(terms.getId()));
    }
}
