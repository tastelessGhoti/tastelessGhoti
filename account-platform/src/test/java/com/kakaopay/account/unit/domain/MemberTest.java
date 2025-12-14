package com.kakaopay.account.unit.domain;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.entity.MemberStatus;
import com.kakaopay.account.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Member 도메인 테스트")
class MemberTest {

    @Nested
    @DisplayName("회원 생성")
    class Create {

        @Test
        @DisplayName("회원 생성 시 기본 상태는 ACTIVE이다")
        void 회원_생성시_기본_상태_ACTIVE() {
            // given & when
            Member member = MemberFixture.createMember();

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {

        @Test
        @DisplayName("ACTIVE 상태의 회원은 탈퇴할 수 있다")
        void ACTIVE_회원_탈퇴_가능() {
            // given
            Member member = MemberFixture.createMember();

            // when
            member.withdraw();

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
            assertThat(member.getWithdrawnAt()).isNotNull();
        }

        @Test
        @DisplayName("SUSPENDED 상태의 회원도 탈퇴할 수 있다")
        void SUSPENDED_회원_탈퇴_가능() {
            // given
            Member member = MemberFixture.createMember();
            member.suspend("테스트 정지");

            // when
            member.withdraw();

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("이미 탈퇴한 회원은 다시 탈퇴할 수 없다")
        void 이미_탈퇴한_회원_재탈퇴_불가() {
            // given
            Member member = MemberFixture.createMember();
            member.withdraw();

            // when & then
            assertThatThrownBy(member::withdraw)
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
    }

    @Nested
    @DisplayName("회원 정지")
    class Suspend {

        @Test
        @DisplayName("회원 정지 시 상태가 SUSPENDED로 변경된다")
        void 회원_정지_상태_변경() {
            // given
            Member member = MemberFixture.createMember();
            String reason = "이상 거래 감지";

            // when
            member.suspend(reason);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
            assertThat(member.getSuspensionReason()).isEqualTo(reason);
            assertThat(member.getSuspendedAt()).isNotNull();
        }

        @Test
        @DisplayName("탈퇴한 회원은 정지할 수 없다")
        void 탈퇴_회원_정지_불가() {
            // given
            Member member = MemberFixture.createMember();
            member.withdraw();

            // when & then
            assertThatThrownBy(() -> member.suspend("정지 사유"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
    }

    @Nested
    @DisplayName("회원 재활성화")
    class Reactivate {

        @Test
        @DisplayName("정지된 회원은 재활성화할 수 있다")
        void 정지_회원_재활성화_가능() {
            // given
            Member member = MemberFixture.createMember();
            member.suspend("정지 사유");

            // when
            member.reactivate();

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.getSuspensionReason()).isNull();
            assertThat(member.getSuspendedAt()).isNull();
        }

        @Test
        @DisplayName("ACTIVE 상태의 회원은 재활성화할 수 없다")
        void ACTIVE_회원_재활성화_불가() {
            // given
            Member member = MemberFixture.createMember();

            // when & then
            assertThatThrownBy(member::reactivate)
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_MEMBER_STATUS);
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class Update {

        @Test
        @DisplayName("전화번호를 변경할 수 있다")
        void 전화번호_변경() {
            // given
            Member member = MemberFixture.createMember();
            String newPhoneNumber = "01098765432";

            // when
            member.updatePhoneNumber(newPhoneNumber);

            // then
            assertThat(member.getPhoneNumber()).isEqualTo(newPhoneNumber);
        }

        @Test
        @DisplayName("이메일을 변경할 수 있다")
        void 이메일_변경() {
            // given
            Member member = MemberFixture.createMember();
            String newEmail = "new@example.com";

            // when
            member.updateEmail(newEmail);

            // then
            assertThat(member.getEmail()).isEqualTo(newEmail);
        }
    }
}
