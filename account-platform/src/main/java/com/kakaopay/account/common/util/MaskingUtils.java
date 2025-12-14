package com.kakaopay.account.common.util;

import org.springframework.util.StringUtils;

/**
 * 개인정보 마스킹 유틸리티
 */
public final class MaskingUtils {

    private MaskingUtils() {
    }

    /**
     * 이름 마스킹 (홍*동)
     */
    public static String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        if (name.length() > 2) {
            return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
        }
        return name;
    }

    /**
     * 전화번호 마스킹 (010-****-1234)
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return phoneNumber;
        }
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() < 10) {
            return phoneNumber;
        }
        return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
    }

    /**
     * 이메일 마스킹 (ho***@example.com)
     */
    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domain;
        }
        return localPart.substring(0, 2) + "***@" + domain;
    }
}
