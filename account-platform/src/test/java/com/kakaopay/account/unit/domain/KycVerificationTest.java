package com.kakaopay.account.unit.domain;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.domain.kyc.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("KycVerification 도메인 테스트")
class KycVerificationTest {

    @Nested
    @DisplayName("KYC 인증 요청")
    class Request {

        @Test
        @DisplayName("KYC 인증 생성 시 초기 상태는 PENDING이다")
        void 초기_상태_PENDING() {
            // given & when
            KycVerification verification = KycVerification.builder()
                    .memberId(1L)
                    .verificationType(KycVerificationType.ID_CARD)
                    .build();

            // then
            assertThat(verification.getStatus()).isEqualTo(KycStatus.PENDING);
            assertThat(verification.getLevel()).isEqualTo(KycLevel.NONE);
        }

        @Test
        @DisplayName("인증 시작 시 상태가 IN_PROGRESS로 변경된다")
        void 인증_시작_상태_변경() {
            // given
            KycVerification verification = KycVerification.builder()
                    .memberId(1L)
                    .verificationType(KycVerificationType.ID_CARD)
                    .build();

            // when
            verification.startVerification("주민등록증", "hashedNumber", "홍길동", "19900101");

            // then
            assertThat(verification.getStatus()).isEqualTo(KycStatus.IN_PROGRESS);
            assertThat(verification.getVerifiedName()).isEqualTo("홍길동");
        }
    }

    @Nested
    @DisplayName("KYC 인증 승인")
    class Approve {

        @Test
        @DisplayName("인증 승인 시 상태가 VERIFIED로 변경되고 레벨이 설정된다")
        void 인증_승인_성공() {
            // given
            KycVerification verification = createInProgressVerification();
            LocalDate expiresAt = LocalDate.now().plusYears(1);

            // when
            verification.approve(KycLevel.LEVEL_2, expiresAt);

            // then
            assertThat(verification.getStatus()).isEqualTo(KycStatus.VERIFIED);
            assertThat(verification.getLevel()).isEqualTo(KycLevel.LEVEL_2);
            assertThat(verification.getVerifiedAt()).isNotNull();
            assertThat(verification.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("이미 인증된 건은 다시 승인할 수 없다")
        void 중복_승인_불가() {
            // given
            KycVerification verification = createInProgressVerification();
            verification.approve(KycLevel.LEVEL_1, LocalDate.now().plusYears(1));

            // when & then
            assertThatThrownBy(() -> verification.approve(KycLevel.LEVEL_2, LocalDate.now().plusYears(1)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.KYC_ALREADY_VERIFIED);
        }
    }

    @Nested
    @DisplayName("KYC 인증 거절")
    class Reject {

        @Test
        @DisplayName("인증 거절 시 상태가 REJECTED로 변경되고 재시도 횟수가 증가한다")
        void 인증_거절_상태_변경() {
            // given
            KycVerification verification = createInProgressVerification();

            // when
            verification.reject("신분증 사진이 불명확합니다");

            // then
            assertThat(verification.getStatus()).isEqualTo(KycStatus.REJECTED);
            assertThat(verification.getRejectionReason()).isEqualTo("신분증 사진이 불명확합니다");
            assertThat(verification.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("3회 거절 후에는 재시도할 수 없다")
        void 최대_재시도_횟수_초과() {
            // given
            KycVerification verification = createInProgressVerification();
            verification.reject("1차 거절");
            verification.reject("2차 거절");
            verification.reject("3차 거절");

            // when & then
            assertThat(verification.canRetry()).isFalse();
            assertThat(verification.getRetryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("KYC 인증 만료")
    class Expiry {

        @Test
        @DisplayName("만료일이 지나면 isExpired가 true를 반환한다")
        void 만료일_경과_확인() {
            // given
            KycVerification verification = createInProgressVerification();
            verification.approve(KycLevel.LEVEL_1, LocalDate.now().minusDays(1));

            // when & then
            assertThat(verification.isExpired()).isTrue();
            assertThat(verification.isVerified()).isFalse();
        }

        @Test
        @DisplayName("만료일 전이면 isVerified가 true를 반환한다")
        void 유효_기간_내_확인() {
            // given
            KycVerification verification = createInProgressVerification();
            verification.approve(KycLevel.LEVEL_1, LocalDate.now().plusYears(1));

            // when & then
            assertThat(verification.isExpired()).isFalse();
            assertThat(verification.isVerified()).isTrue();
        }
    }

    private KycVerification createInProgressVerification() {
        KycVerification verification = KycVerification.builder()
                .memberId(1L)
                .verificationType(KycVerificationType.ID_CARD)
                .build();
        verification.startVerification("주민등록증", "hashedNumber", "홍길동", "19900101");
        return verification;
    }
}
