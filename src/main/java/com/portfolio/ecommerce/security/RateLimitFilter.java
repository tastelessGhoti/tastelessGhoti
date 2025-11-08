package com.portfolio.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.ecommerce.config.RateLimitConfig;
import com.portfolio.ecommerce.exception.ErrorResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Rate Limiting 필터
 * API 호출 횟수 제한
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> rateLimitBuckets;
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Rate Limit 제외 경로
        String requestURI = request.getRequestURI();
        if (isExcludedPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 클라이언트 IP 추출
        String clientIp = getClientIp(request);

        // 로그인 API는 더 엄격한 제한
        Bucket bucket;
        if (requestURI.contains("/api/auth/login")) {
            bucket = rateLimitBuckets.computeIfAbsent(
                "login:" + clientIp,
                key -> rateLimitConfig.createLoginBucket()
            );
        } else {
            bucket = rateLimitBuckets.computeIfAbsent(
                clientIp,
                key -> rateLimitConfig.createDefaultBucket()
            );
        }

        // 토큰 소비 시도
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // 남은 요청 수를 헤더에 추가
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Rate Limit 초과
            log.warn("Rate limit exceeded for IP: {}, Path: {}", clientIp, requestURI);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = new ErrorResponse(
                "RATE_LIMIT_EXCEEDED",
                "요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.TOO_MANY_REQUESTS.value()
            );

            response.addHeader("X-Rate-Limit-Retry-After-Seconds",
                String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    /**
     * Rate Limit 제외 경로 확인
     */
    private boolean isExcludedPath(String requestURI) {
        return requestURI.startsWith("/h2-console") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/api-docs") ||
               requestURI.startsWith("/actuator/health");
    }

    /**
     * 클라이언트 IP 추출
     * 프록시 환경을 고려한 실제 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 여러 IP가 있는 경우 첫 번째 IP 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
