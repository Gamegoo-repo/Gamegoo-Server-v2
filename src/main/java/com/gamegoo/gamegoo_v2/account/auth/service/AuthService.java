package com.gamegoo.gamegoo_v2.account.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void deleteRefreshToken(Member member) {
        RefreshToken refreshToken = refreshTokenRepository.findByMember(member)
                .orElseThrow(); // 리프레시 토큰이 없을 경우 에러를 발생시킬 필요가 없음. 이미 삭제된 상태

        refreshTokenRepository.delete(refreshToken);
    }
    /**
     * 비밀번호 검증
     *
     * @param member   회원
     * @param password 비밀번호
     */
    public void verifyPassword(Member member, String password) {
        if (!PasswordUtil.matchesPassword(password, member.getPassword())) {
            throw new MemberException(ErrorCode.INVALID_PASSWORD);
        }
    }
}
