package com.kakaopay.account.domain.auth.entity;

import com.kakaopay.account.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "login_history", indexes = {
        @Index(name = "idx_login_history_member", columnList = "member_id, created_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_result", nullable = false, length = 20)
    private LoginResult loginResult;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    @Builder
    private LoginHistory(Long memberId, String ipAddress, String userAgent,
                         String deviceInfo, LoginResult loginResult, String failureReason) {
        this.memberId = memberId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceInfo = deviceInfo;
        this.loginResult = loginResult;
        this.failureReason = failureReason;
    }

    public static LoginHistory success(Long memberId, String ipAddress,
                                        String userAgent, String deviceInfo) {
        return LoginHistory.builder()
                .memberId(memberId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceInfo(deviceInfo)
                .loginResult(LoginResult.SUCCESS)
                .build();
    }

    public static LoginHistory failure(Long memberId, String ipAddress,
                                        String userAgent, String failureReason) {
        return LoginHistory.builder()
                .memberId(memberId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginResult(LoginResult.FAILURE)
                .failureReason(failureReason)
                .build();
    }
}
