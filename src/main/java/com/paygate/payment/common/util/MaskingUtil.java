package com.paygate.payment.common.util;

import org.springframework.util.StringUtils;

/**
 * 민감정보 마스킹 유틸리티.
 * 로그 출력이나 응답 시 카드번호 등의 민감정보 보호.
 */
public final class MaskingUtil {

    private MaskingUtil() {
    }

    /**
     * 카드번호 마스킹.
     * 앞 6자리, 뒤 4자리만 노출.
     */
    public static String maskCardNumber(String cardNumber) {
        if (!StringUtils.hasText(cardNumber) || cardNumber.length() < 14) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        int len = cleaned.length();
        return cleaned.substring(0, 6) + "****" + cleaned.substring(len - 4);
    }

    /**
     * 이메일 마스킹.
     * 앞 3자리 + *** + @도메인
     */
    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 3) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 3) + "***" + email.substring(atIndex);
    }

    /**
     * 전화번호 마스킹.
     * 가운데 번호 마스킹.
     */
    public static String maskPhoneNumber(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 10) {
            return "***";
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        int len = cleaned.length();
        return cleaned.substring(0, 3) + "****" + cleaned.substring(len - 4);
    }
}
