package com.gamegoo.gamegoo_v2.account.auth.service;

import com.gamegoo.gamegoo_v2.account.auth.domain.RefreshToken;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.auth.repository.RefreshTokenRepository;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.exception.JwtAuthException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    /**
     * 리프레시 토큰 제거
     *
     * @param member 로그아웃한 회원
     */
    @Transactional
    public void deleteRefreshToken(Member member) {
        refreshTokenRepository.findByMember(member).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * 리프레시 토큰 생성
     *
     * @param member       로그인한 회원
     * @param refreshToken 리프레시토큰 정보
     */
    @Transactional
    public void addRefreshToken(Member member, String refreshToken) {
        // refresh token DB에 저장하기
        refreshTokenRepository.save(RefreshToken.create(refreshToken, member));
    }

    /**
     * 리프레시 토큰 검증
     *
     * @param refreshToken 리프레시 토큰
     */
    public void verifyRefreshToken(String refreshToken) {
        // DB 검증
        if (refreshTokenRepository.findByRefreshToken(refreshToken).isEmpty()) {
            throw new JwtAuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // JWT 검증
        jwtProvider.validateToken(refreshToken);
    }

}
