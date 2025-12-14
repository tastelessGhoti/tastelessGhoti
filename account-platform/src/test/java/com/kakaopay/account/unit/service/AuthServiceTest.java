package com.kakaopay.account.unit.service;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.config.JwtConfig;
import com.kakaopay.account.domain.auth.dto.LoginRequest;
import com.kakaopay.account.domain.auth.dto.TokenResponse;
import com.kakaopay.account.domain.auth.entity.LoginResult;
import com.kakaopay.account.domain.auth.repository.LoginHistoryRepository;
import com.kakaopay.account.domain.auth.repository.RefreshTokenRepository;
import com.kakaopay.account.domain.auth.service.AuthService;
import com.kakaopay.account.domain.auth.service.JwtTokenProvider;
import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.service.MemberService;
import com.kakaopay.account.fixture.MemberFixture;
import com.kakaopay.account.infrastructure.redis.TokenBlacklistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberService memberService;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginHistoryRepository loginHistoryRepository;
    @Mock
    private TokenBlacklistService blacklistService;
    @Mock
    private JwtConfig jwtConfig;

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("정상적인 CI로 로그인하면 토큰이 발급된다")
        void 정상_로그인_성공() {
            // given
            String ci = "TESTCI1234567890123456789012345678901234567890123456789012345678901234567890123456";
            LoginRequest request = LoginRequest.builder()
                    .ci(ci)
                    .deviceInfo("iPhone 15")
                    .build();

            Member member = MemberFixture.createMemberWithId(1L);
            String accessToken = "access-token-value";
            String refreshToken = "refresh-token-value";

            given(memberService.findMemberByCi(ci)).willReturn(member);
            given(loginHistoryRepository.countRecentLoginsByResult(anyLong(), eq(LoginResult.FAILURE), any(LocalDateTime.class)))
                    .willReturn(0);
            given(tokenProvider.createAccessToken(member.getId())).willReturn(accessToken);
            given(tokenProvider.createRefreshToken(member.getId())).willReturn(refreshToken);
            given(jwtConfig.getAccessTokenValidity()).willReturn(1800000L);
            given(jwtConfig.getRefreshTokenValidity()).willReturn(604800000L);

            // when
            TokenResponse response = authService.login(request, "127.0.0.1", "Mozilla/5.0");

            // then
            assertThat(response.getAccessToken()).isEqualTo(accessToken);
            assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("존재하지 않는 CI로 로그인하면 인증 실패 예외가 발생한다")
        void 존재하지_않는_CI_로그인_실패() {
            // given
            String ci = "INVALID_CI_VALUE_123456789012345678901234567890123456789012345678901234567890";
            LoginRequest request = LoginRequest.builder()
                    .ci(ci)
                    .build();

            given(memberService.findMemberByCi(ci))
                    .willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "Mozilla/5.0"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        }

        @Test
        @DisplayName("로그인 시도 횟수를 초과하면 인증 실패 예외가 발생한다")
        void 로그인_시도_횟수_초과_실패() {
            // given
            String ci = "TESTCI1234567890123456789012345678901234567890123456789012345678901234567890123456";
            LoginRequest request = LoginRequest.builder()
                    .ci(ci)
                    .build();

            Member member = MemberFixture.createMemberWithId(1L);
            given(memberService.findMemberByCi(ci)).willReturn(member);
            given(loginHistoryRepository.countRecentLoginsByResult(anyLong(), eq(LoginResult.FAILURE), any(LocalDateTime.class)))
                    .willReturn(5);

            // when & then
            assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "Mozilla/5.0"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        }

        @Test
        @DisplayName("탈퇴한 회원이 로그인하면 인증 실패 예외가 발생한다")
        void 탈퇴_회원_로그인_실패() {
            // given
            String ci = "TESTCI1234567890123456789012345678901234567890123456789012345678901234567890123456";
            LoginRequest request = LoginRequest.builder()
                    .ci(ci)
                    .build();

            Member member = MemberFixture.createMemberWithId(1L);
            member.withdraw();

            given(memberService.findMemberByCi(ci)).willReturn(member);
            given(loginHistoryRepository.countRecentLoginsByResult(anyLong(), eq(LoginResult.FAILURE), any(LocalDateTime.class)))
                    .willReturn(0);

            // when & then
            assertThatThrownBy(() -> authService.login(request, "127.0.0.1", "Mozilla/5.0"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("로그아웃하면 토큰이 블랙리스트에 추가되고 리프레시 토큰이 삭제된다")
        void 로그아웃_성공() {
            // given
            String accessToken = "valid-access-token";
            Long memberId = 1L;

            given(tokenProvider.getRemainingExpiration(accessToken)).willReturn(1000L);

            // when
            authService.logout(accessToken, memberId);

            // then
            verify(blacklistService).addToBlacklist(accessToken, 1000L);
            verify(refreshTokenRepository).deleteAllByMemberId(memberId);
        }
    }
}
