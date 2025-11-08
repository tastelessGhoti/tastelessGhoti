package com.portfolio.ecommerce.domain.user.service;

import com.portfolio.ecommerce.domain.user.User;
import com.portfolio.ecommerce.domain.user.UserRepository;
import com.portfolio.ecommerce.domain.user.UserRole;
import com.portfolio.ecommerce.domain.user.dto.LoginRequest;
import com.portfolio.ecommerce.domain.user.dto.SignupRequest;
import com.portfolio.ecommerce.domain.user.dto.TokenResponse;
import com.portfolio.ecommerce.domain.user.dto.UserResponse;
import com.portfolio.ecommerce.exception.BusinessException;
import com.portfolio.ecommerce.exception.ErrorCode;
import com.portfolio.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 인증 서비스
 * 회원가입, 로그인 등 인증 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS,
                "이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 사용자 생성
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .phoneNumber(request.getPhoneNumber())
            .address(request.getAddress())
            .role(UserRole.USER)
            .build();

        User savedUser = userRepository.save(user);
        log.info("새로운 사용자 등록: {}", savedUser.getEmail());

        return UserResponse.from(savedUser);
    }

    /**
     * 로그인
     */
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                "존재하지 않는 사용자입니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD,
                "비밀번호가 일치하지 않습니다.");
        }

        // 계정 활성 상태 확인
        if (user.getStatus() != com.portfolio.ecommerce.domain.user.UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                "비활성화된 계정입니다.");
        }

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getKey()))
        );

        // JWT 토큰 생성
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        log.info("사용자 로그인: {}", user.getEmail());

        return new TokenResponse(accessToken, refreshToken);
    }
}
