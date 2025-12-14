package com.kakaopay.account.domain.auth.service;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.config.JwtConfig;
import com.kakaopay.account.domain.auth.dto.LoginRequest;
import com.kakaopay.account.domain.auth.dto.TokenRefreshRequest;
import com.kakaopay.account.domain.auth.dto.TokenResponse;
import com.kakaopay.account.domain.auth.entity.LoginHistory;
import com.kakaopay.account.domain.auth.entity.LoginResult;
import com.kakaopay.account.domain.auth.entity.RefreshToken;
import com.kakaopay.account.domain.auth.repository.LoginHistoryRepository;
import com.kakaopay.account.domain.auth.repository.RefreshTokenRepository;
import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.service.MemberService;
import com.kakaopay.account.infrastructure.redis.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_ATTEMPT_WINDOW_MINUTES = 30;

    private final MemberService memberService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final TokenBlacklistService blacklistService;
    private final JwtConfig jwtConfig;

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Member member;
        try {
            member = memberService.findMemberByCi(request.getCi());
        } catch (BusinessException e) {
            log.warn("로그인 실패 - 존재하지 않는 회원: ci={}", maskCi(request.getCi()));
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        validateLoginAttempts(member.getId());
        validateMemberStatus(member);

        String accessToken = tokenProvider.createAccessToken(member.getId());
        String refreshToken = tokenProvider.createRefreshToken(member.getId());

        saveRefreshToken(member.getId(), refreshToken, request.getDeviceInfo(), ipAddress);
        saveLoginHistory(member.getId(), ipAddress, userAgent, request.getDeviceInfo(), true, null);

        member.updateLastLoginAt();

        log.info("로그인 성공: memberId={}", member.getId());
        return TokenResponse.of(accessToken, refreshToken, jwtConfig.getAccessTokenValidity() / 1000);
    }

    @Transactional
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        tokenProvider.validateToken(refreshTokenValue);
        Long memberId = tokenProvider.getMemberIdFromToken(refreshTokenValue);

        RefreshToken storedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_NOT_FOUND));

        if (!storedToken.matches(refreshTokenValue)) {
            refreshTokenRepository.deleteAllByMemberId(memberId);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        String newAccessToken = tokenProvider.createAccessToken(memberId);
        String newRefreshToken = tokenProvider.createRefreshToken(memberId);

        RefreshToken newToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .memberId(memberId)
                .tokenValue(newRefreshToken)
                .deviceInfo(storedToken.getDeviceInfo())
                .ipAddress(storedToken.getIpAddress())
                .expiration(jwtConfig.getRefreshTokenValidity())
                .build();

        refreshTokenRepository.delete(storedToken);
        refreshTokenRepository.save(newToken);

        log.debug("토큰 갱신 완료: memberId={}", memberId);
        return TokenResponse.of(newAccessToken, newRefreshToken, jwtConfig.getAccessTokenValidity() / 1000);
    }

    @Transactional
    public void logout(String accessToken, Long memberId) {
        long remainingExpiration = tokenProvider.getRemainingExpiration(accessToken);
        if (remainingExpiration > 0) {
            blacklistService.addToBlacklist(accessToken, remainingExpiration);
        }

        refreshTokenRepository.deleteAllByMemberId(memberId);
        log.info("로그아웃 처리 완료: memberId={}", memberId);
    }

    private void validateLoginAttempts(Long memberId) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOGIN_ATTEMPT_WINDOW_MINUTES);
        int failCount = loginHistoryRepository.countRecentLoginsByResult(
                memberId, LoginResult.FAILURE, since
        );

        if (failCount >= MAX_LOGIN_ATTEMPTS) {
            log.warn("로그인 시도 횟수 초과: memberId={}, failCount={}", memberId, failCount);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED,
                    "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private void validateMemberStatus(Member member) {
        if (!member.getStatus().canLogin()) {
            String reason = switch (member.getStatus()) {
                case WITHDRAWN -> "탈퇴한 회원입니다";
                case SUSPENDED -> "이용 정지된 회원입니다";
                default -> "로그인할 수 없는 상태입니다";
            };
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, reason);
        }
    }

    private void saveRefreshToken(Long memberId, String tokenValue, String deviceInfo, String ipAddress) {
        refreshTokenRepository.deleteAllByMemberId(memberId);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .memberId(memberId)
                .tokenValue(tokenValue)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiration(jwtConfig.getRefreshTokenValidity())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private void saveLoginHistory(Long memberId, String ipAddress, String userAgent,
                                  String deviceInfo, boolean success, String failureReason) {
        LoginHistory history = success
                ? LoginHistory.success(memberId, ipAddress, userAgent, deviceInfo)
                : LoginHistory.failure(memberId, ipAddress, userAgent, failureReason);

        loginHistoryRepository.save(history);
    }

    private String maskCi(String ci) {
        if (ci == null || ci.length() < 10) return "***";
        return ci.substring(0, 5) + "***" + ci.substring(ci.length() - 5);
    }
}
