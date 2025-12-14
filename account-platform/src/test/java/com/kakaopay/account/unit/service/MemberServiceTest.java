package com.kakaopay.account.unit.service;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.domain.member.dto.MemberResponse;
import com.kakaopay.account.domain.member.dto.SignUpRequest;
import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.entity.MemberStatus;
import com.kakaopay.account.domain.member.repository.MemberRepository;
import com.kakaopay.account.domain.member.service.MemberService;
import com.kakaopay.account.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("회원 가입")
    class SignUp {

        @Test
        @DisplayName("정상적인 정보로 회원가입하면 성공한다")
        void 정상_회원가입_성공() {
            // given
            SignUpRequest request = MemberFixture.createSignUpRequest();
            Member savedMember = MemberFixture.createMemberWithId(1L);

            given(memberRepository.existsByCi(request.getCi())).willReturn(false);
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);

            // when
            MemberResponse response = memberService.signUp(request);

            // then
            assertThat(response.getMemberId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("이미 가입된 CI로 회원가입하면 실패한다")
        void 중복_CI_회원가입_실패() {
            // given
            SignUpRequest request = MemberFixture.createSignUpRequest();
            given(memberRepository.existsByCi(request.getCi())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.signUp(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_MEMBER);
        }
    }

    @Nested
    @DisplayName("회원 조회")
    class GetMember {

        @Test
        @DisplayName("존재하는 회원 ID로 조회하면 회원 정보를 반환한다")
        void 존재하는_회원_조회_성공() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createMemberWithId(memberId);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            MemberResponse response = memberService.getMember(memberId);

            // then
            assertThat(response.getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 조회하면 예외가 발생한다")
        void 존재하지_않는_회원_조회_실패() {
            // given
            Long memberId = 999L;
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMember(memberId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {

        @Test
        @DisplayName("활성 상태의 회원이 탈퇴하면 상태가 WITHDRAWN으로 변경된다")
        void 활성_회원_탈퇴_성공() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createMemberWithId(memberId);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            memberService.withdraw(memberId);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
            assertThat(member.getWithdrawnAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 탈퇴한 회원이 다시 탈퇴하면 예외가 발생한다")
        void 이미_탈퇴한_회원_재탈퇴_실패() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createMemberWithId(memberId);
            member.withdraw();
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> memberService.withdraw(memberId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
    }

    @Nested
    @DisplayName("회원 정지/해제")
    class SuspendAndReactivate {

        @Test
        @DisplayName("회원을 정지하면 상태가 SUSPENDED로 변경되고 사유가 기록된다")
        void 회원_정지_성공() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createMemberWithId(memberId);
            String reason = "부정 사용 의심";
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            memberService.suspendMember(memberId, reason);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
            assertThat(member.getSuspensionReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("정지된 회원을 재활성화하면 ACTIVE 상태로 변경된다")
        void 회원_재활성화_성공() {
            // given
            Long memberId = 1L;
            Member member = MemberFixture.createMemberWithId(memberId);
            member.suspend("테스트 정지");
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            memberService.reactivateMember(memberId);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.getSuspensionReason()).isNull();
        }
    }
}
