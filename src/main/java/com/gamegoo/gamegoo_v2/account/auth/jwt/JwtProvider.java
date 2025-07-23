package com.gamegoo.gamegoo_v2.account.auth.jwt;

import com.gamegoo.gamegoo_v2.core.exception.JwtAuthException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpTime;

    @Value("${jwt.refresh_expiration_day}")
    private long refreshExpireDay;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access_expiration_time}") long accessTokenExpTime) {
        key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessTokenExpTime = accessTokenExpTime;
    }

    /**
     * access token 생성 메소드
     *
     * @param memberId
     * @return
     */
    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessTokenExpTime);
    }

    /**
     * refresh token 생성 메소드
     *
     * @param memberId
     * @return
     */
    public String createRefreshToken(Long memberId) {
        // refreshExpireDay를 밀리초 단위로 변환
        long refreshExpireTimeInMillis = refreshExpireDay * 24 * 60 * 60 * 1000;

        return createToken(memberId, refreshExpireTimeInMillis);
    }

    /**
     * token claim에서 만료 시간 추출 메소드
     *
     * @param token
     * @return
     */
    public Long getTokenExpirationTime(String token) {
        return parseClaims(token).getExpiration().getTime();
    }

    /**
     * token claim에서 memberId 추출 메소드
     *
     * @param token
     * @return
     */
    public Long getMemberId(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }

    /**
     * request의 header에서 token 값 추출 메소드
     *
     * @param request
     * @return
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * JWT 검증
     *
     * @param token
     * @return IsValidate
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key) // 서명 키 설정
                    .build()
                    .parse(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new JwtAuthException(ErrorCode.INVALID_SIGNATURE);
        } catch (MalformedJwtException e) {
            throw new JwtAuthException(ErrorCode.MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthException(ErrorCode.EXPIRED_JWT_EXCEPTION);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthException(ErrorCode.INVALID_CLAIMS);
        }
    }

    /**
     * token 생성 메소드
     *
     * @param memberId
     * @param expireTime
     * @return
     */
    private String createToken(Long memberId, Long expireTime) {

        long now = (new Date()).getTime();
        Date validity = new Date(now + expireTime);

        return Jwts.builder()
                .claim("memberId", memberId)
                .issuedAt(new Date(now))
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    /**
     * JWT Claims 추출 메소드
     *
     * @param accessToken
     * @return JWT Claims
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(key) // 서명 키 설정
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
