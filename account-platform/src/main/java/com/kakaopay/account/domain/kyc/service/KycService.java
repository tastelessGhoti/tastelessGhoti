package com.kakaopay.account.domain.kyc.service;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.domain.kyc.dto.KycVerificationRequest;
import com.kakaopay.account.domain.kyc.dto.KycVerificationResponse;
import com.kakaopay.account.domain.kyc.entity.KycLevel;
import com.kakaopay.account.domain.kyc.entity.KycStatus;
import com.kakaopay.account.domain.kyc.entity.KycVerification;
import com.kakaopay.account.domain.kyc.repository.KycVerificationRepository;
import com.kakaopay.account.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private static final int KYC_VALIDITY_YEARS = 1;

    private final KycVerificationRepository kycRepository;
    private final MemberService memberService;

    @Transactional
    public KycVerificationResponse requestVerification(Long memberId, KycVerificationRequest request) {
        memberService.findMemberById(memberId);

        checkExistingVerification(memberId);

        KycVerification verification = KycVerification.builder()
                .memberId(memberId)
                .verificationType(request.getVerificationType())
                .build();

        String hashedIdCardNumber = hashIdCardNumber(request.getIdCardNumber());
        verification.startVerification(
                request.getIdCardType(),
                hashedIdCardNumber,
                request.getName(),
                request.getBirthDate()
        );

        KycVerification saved = kycRepository.save(verification);
        log.info("KYC 인증 요청 접수: memberId={}, verificationId={}", memberId, saved.getId());

        return KycVerificationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public KycVerificationResponse getVerificationStatus(Long memberId) {
        KycVerification verification = kycRepository.findLatestVerified(memberId)
                .or(() -> kycRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream().findFirst())
                .orElseThrow(() -> new BusinessException(ErrorCode.KYC_NOT_FOUND));

        if (verification.isExpired()) {
            return KycVerificationResponse.builder()
                    .verificationId(verification.getId())
                    .status(KycStatus.EXPIRED)
                    .level(KycLevel.NONE)
                    .canRetry(true)
                    .build();
        }

        return KycVerificationResponse.from(verification);
    }

    @Transactional(readOnly = true)
    public List<KycVerificationResponse> getVerificationHistory(Long memberId) {
        return kycRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(KycVerificationResponse::from)
                .toList();
    }

    @Transactional
    public KycVerificationResponse approveVerification(Long verificationId, KycLevel level) {
        KycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.KYC_NOT_FOUND));

        LocalDate expiresAt = LocalDate.now().plusYears(KYC_VALIDITY_YEARS);
        verification.approve(level, expiresAt);

        log.info("KYC 인증 승인: verificationId={}, level={}", verificationId, level);
        return KycVerificationResponse.from(verification);
    }

    @Transactional
    public KycVerificationResponse rejectVerification(Long verificationId, String reason) {
        KycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.KYC_NOT_FOUND));

        verification.reject(reason);
        log.info("KYC 인증 거절: verificationId={}, reason={}", verificationId, reason);

        return KycVerificationResponse.from(verification);
    }

    @Transactional(readOnly = true)
    public boolean isKycVerified(Long memberId) {
        return kycRepository.findLatestVerified(memberId)
                .map(v -> !v.isExpired())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public KycLevel getMemberKycLevel(Long memberId) {
        return kycRepository.findLatestVerified(memberId)
                .filter(v -> !v.isExpired())
                .map(KycVerification::getLevel)
                .orElse(KycLevel.NONE);
    }

    private void checkExistingVerification(Long memberId) {
        if (kycRepository.existsByMemberIdAndStatus(memberId, KycStatus.IN_PROGRESS)) {
            throw new BusinessException(ErrorCode.KYC_ALREADY_VERIFIED, "이미 진행 중인 인증이 있습니다");
        }

        kycRepository.findLatestVerified(memberId)
                .filter(v -> !v.isExpired())
                .ifPresent(v -> {
                    throw new BusinessException(ErrorCode.KYC_ALREADY_VERIFIED);
                });
    }

    private String hashIdCardNumber(String idCardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(idCardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
