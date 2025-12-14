package com.kakaopay.account.fixture;

import com.kakaopay.account.domain.member.dto.SignUpRequest;
import com.kakaopay.account.domain.member.entity.Member;

import java.lang.reflect.Field;

public class MemberFixture {

    private static final String DEFAULT_CI = "TESTCI1234567890123456789012345678901234567890123456789012345678901234567890123456";
    private static final String DEFAULT_NAME = "홍길동";
    private static final String DEFAULT_PHONE = "01012345678";

    public static Member createMember() {
        return Member.builder()
                .ci(DEFAULT_CI)
                .di("TESTDI123456")
                .name(DEFAULT_NAME)
                .phoneNumber(DEFAULT_PHONE)
                .email("test@example.com")
                .birthDate("19900101")
                .build();
    }

    public static Member createMember(String ci, String name) {
        return Member.builder()
                .ci(ci)
                .di("DI_" + ci.substring(0, 10))
                .name(name)
                .phoneNumber(DEFAULT_PHONE)
                .email(name + "@example.com")
                .birthDate("19900101")
                .build();
    }

    public static Member createMemberWithId(Long id) {
        Member member = createMember();
        setField(member, "id", id);
        return member;
    }

    public static SignUpRequest createSignUpRequest() {
        return SignUpRequest.builder()
                .ci(DEFAULT_CI)
                .di("TESTDI123456")
                .name(DEFAULT_NAME)
                .phoneNumber(DEFAULT_PHONE)
                .email("test@example.com")
                .birthDate("19900101")
                .build();
    }

    public static SignUpRequest createSignUpRequest(String ci) {
        return SignUpRequest.builder()
                .ci(ci)
                .di("DI_" + ci.substring(0, 10))
                .name(DEFAULT_NAME)
                .phoneNumber(DEFAULT_PHONE)
                .email("test@example.com")
                .birthDate("19900101")
                .build();
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
