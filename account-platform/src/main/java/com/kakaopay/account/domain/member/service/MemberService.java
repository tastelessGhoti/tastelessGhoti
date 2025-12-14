package com.kakaopay.account.domain.member.service;

import com.kakaopay.account.common.exception.BusinessException;
import com.kakaopay.account.common.exception.ErrorCode;
import com.kakaopay.account.domain.member.dto.*;
import com.kakaopay.account.domain.member.entity.Member;
import com.kakaopay.account.domain.member.entity.MemberStatus;
import com.kakaopay.account.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        validateDuplicateMember(request.getCi());

        Member member = Member.builder()
                .ci(request.getCi())
                .di(request.getDi())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("신규 회원 가입 완료: memberId={}", savedMember.getId());

        return MemberResponse.from(savedMember);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    /**
     * 내부 서비스 전용 - 마스킹 없는 회원 정보 조회
     */
    @Transactional(readOnly = true)
    public MemberResponse getMemberForInternal(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberResponse.forInternal(member);
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> searchMembers(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchMembers(condition, pageable)
                .map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = findMemberById(memberId);
        validateMemberStatus(member);

        if (StringUtils.hasText(request.getPhoneNumber())) {
            member.updatePhoneNumber(request.getPhoneNumber());
        }
        if (StringUtils.hasText(request.getEmail())) {
            member.updateEmail(request.getEmail());
        }

        log.info("회원 정보 수정 완료: memberId={}", memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = findMemberById(memberId);
        member.withdraw();
        log.info("회원 탈퇴 처리 완료: memberId={}", memberId);
    }

    @Transactional
    public void suspendMember(Long memberId, String reason) {
        Member member = findMemberById(memberId);
        member.suspend(reason);
        log.info("회원 이용 정지 처리: memberId={}, reason={}", memberId, reason);
    }

    @Transactional
    public void reactivateMember(Long memberId) {
        Member member = findMemberById(memberId);
        member.reactivate();
        log.info("회원 정지 해제 처리: memberId={}", memberId);
    }

    @Transactional(readOnly = true)
    public boolean existsByCi(String ci) {
        return memberRepository.existsByCi(ci);
    }

    @Transactional(readOnly = true)
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Member findMemberByCi(String ci) {
        return memberRepository.findByCi(ci)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateDuplicateMember(String ci) {
        if (memberRepository.existsByCi(ci)) {
            throw new BusinessException(ErrorCode.DUPLICATE_MEMBER);
        }
    }

    private void validateMemberStatus(Member member) {
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.MEMBER_SUSPENDED);
        }
    }
}
