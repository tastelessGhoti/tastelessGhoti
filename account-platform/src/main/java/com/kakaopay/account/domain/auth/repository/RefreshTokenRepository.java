package com.kakaopay.account.domain.auth.repository;

import com.kakaopay.account.domain.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByMemberId(Long memberId);

    List<RefreshToken> findAllByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
