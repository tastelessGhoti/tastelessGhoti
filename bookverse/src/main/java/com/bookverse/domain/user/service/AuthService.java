package com.bookverse.domain.user.service;

import com.bookverse.domain.user.dto.SignInRequest;
import com.bookverse.domain.user.dto.SignUpRequest;
import com.bookverse.domain.user.dto.TokenResponse;
import com.bookverse.domain.user.entity.User;
import com.bookverse.domain.user.entity.UserStatus;
import com.bookverse.domain.user.repository.UserRepository;
import com.bookverse.exception.BusinessException;
import com.bookverse.exception.ErrorCode;
import com.bookverse.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직 처리 서비스
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public Long signUp(SignUpRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = request.toEntity(encodedPassword);
        User savedUser = userRepository.save(user);

        log.info("새로운 사용자 가입: {}", savedUser.getEmail());
        return savedUser.getId();
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenResponse signIn(SignInRequest request) {
        // 사용자 조회 (활성 상태만)
        User user = userRepository.findByEmailAndStatus(request.getEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        log.info("사용자 로그인: {}", user.getEmail());
        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }
}
