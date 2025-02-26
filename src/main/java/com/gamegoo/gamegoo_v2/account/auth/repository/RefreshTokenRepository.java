package com.gamegoo.gamegoo_v2.account.auth.repository;

import com.gamegoo.gamegoo_v2.account.auth.domain.RefreshToken;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMember(Member member);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
