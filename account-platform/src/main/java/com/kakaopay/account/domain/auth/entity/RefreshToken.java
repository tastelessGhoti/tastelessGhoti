package com.kakaopay.account.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "refresh_token")
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private Long memberId;

    private String tokenValue;

    private String deviceInfo;

    private String ipAddress;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long expiration;

    public boolean matches(String tokenValue) {
        return this.tokenValue.equals(tokenValue);
    }
}
