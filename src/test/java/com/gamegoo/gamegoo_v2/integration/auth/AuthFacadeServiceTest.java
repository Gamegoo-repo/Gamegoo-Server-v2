package com.gamegoo.gamegoo_v2.integration.auth;

import com.gamegoo.gamegoo_v2.account.auth.domain.RefreshToken;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.LoginRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.request.RefreshTokenRequest;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.LoginResponse;
import com.gamegoo.gamegoo_v2.account.auth.dto.response.RefreshTokenResponse;
import com.gamegoo.gamegoo_v2.account.auth.jwt.JwtProvider;
import com.gamegoo.gamegoo_v2.account.auth.repository.RefreshTokenRepository;
import com.gamegoo.gamegoo_v2.account.auth.service.AuthFacadeService;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.exception.JwtAuthException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.utils.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AuthFacadeServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private Member member;

    private static final String EMAIL = "test@gmail.com";
    private static final String NOTFOUND_EMAIL = "notfound@gmail.com";
    private static final String PASSWORD = "password";
    private static final String INVALID_PASSWORD = "invalidpassword";
    private static final String GAMENAME = "test1";
    private static final String INVALID_REFRESH_TOKEN = "invalidrefreshtoken";

    @Autowired
    private AuthFacadeService authFacadeService;

    @BeforeEach
    void setUp() {
        // Member 테스트용 객체 생성
        member = createMember(EMAIL, GAMENAME, PasswordUtil.encodePassword(PASSWORD));
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @DisplayName("로그인 성공")
        @Test
        void login() {
            // given
            LoginRequest loginRequest = LoginRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .build();

            // when
            LoginResponse response = authFacadeService.login(loginRequest);

            // then
            // response 검증
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(jwtProvider.getMemberId(response.getAccessToken()));
            assertThat(response.getId()).isEqualTo(jwtProvider.getMemberId(response.getRefreshToken()));
            assertThat(response.getId()).isEqualTo(member.getId());
            assertThat(response.getName()).isEqualTo(member.getGameName());
            assertThat(response.getProfileImage()).isEqualTo(member.getProfileImage());

            RefreshToken refreshToken = refreshTokenRepository.findByMember(member).orElseThrow();
            Long tokenExpirationTime = jwtProvider.getTokenExpirationTime(refreshToken.getRefreshToken()); // 만료 시간
            checkExpirationTime(tokenExpirationTime);
        }


        @DisplayName("로그인 실패 : 없는 사용자일 경우")
        @Test
        void loginMemberNotFound() {
            // given
            LoginRequest loginRequest = LoginRequest.builder()
                    .email(NOTFOUND_EMAIL)
                    .password(PASSWORD)
                    .build();

            // when // then
            assertThatThrownBy(() -> authFacadeService.login(loginRequest))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("로그인 실패 : 비밀번호가 틀릴 경우")
        @Test
        void loginInvalidPassword() {
            // given
            LoginRequest loginRequest = LoginRequest.builder()
                    .email(EMAIL)
                    .password(INVALID_PASSWORD)
                    .build();

            // when // then
            assertThatThrownBy(() -> authFacadeService.login(loginRequest))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.INVALID_PASSWORD.getMessage());
        }


    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout() {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();
        authFacadeService.login(loginRequest);

        // when
        String response = authFacadeService.logout(member);

        // then
        assertThat(response).isNotNull();
        assertThat(refreshTokenRepository.findByMember(member).isPresent()).isFalse();
    }

    @Nested
    @DisplayName("리프레시 토큰 테스트")
    class RefreshTokenTest {

        @DisplayName("리프레시 토큰으로 다른 토큰 업데이트 성공")
        @Test
        void updateToken() {
            // given
            String token = jwtProvider.createRefreshToken(member.getId());
            RefreshToken refreshToken = RefreshToken.builder()
                    .refreshToken(token)
                    .member(member)
                    .build();
            refreshTokenRepository.save(refreshToken);

            RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken(token).build();

            // when
            RefreshTokenResponse refreshTokenResponse = authFacadeService.updateToken(refreshTokenRequest);

            // then
            Optional<RefreshToken> result = refreshTokenRepository.findByMember(member);
            assertThat(result).isPresent();

            Long jwtId = jwtProvider.getMemberId(refreshTokenResponse.getAccessToken());
            Long memberId = member.getId();
            assertThat(jwtId).isEqualTo(memberId);

            Long responseId = refreshTokenResponse.getId();
            assertThat(responseId).isEqualTo(memberId);

            Long tokenExpirationTime = jwtProvider.getTokenExpirationTime(result.get().getRefreshToken());
            checkExpirationTime(tokenExpirationTime);

        }

        @DisplayName("리프레시 토큰으로 업데이트 실패 : 리프레시 토큰이 올바르지 못할 경우")
        @Test
        void updateTokenWithInvalidRefreshToken() {
            // given
            String token = jwtProvider.createRefreshToken(member.getId());
            RefreshToken refreshToken = RefreshToken.builder()
                    .refreshToken(token)
                    .member(member)
                    .build();
            refreshTokenRepository.save(refreshToken);

            RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder().refreshToken(
                    INVALID_REFRESH_TOKEN).build();

            // when
            assertThatThrownBy(() -> authFacadeService.updateToken(refreshTokenRequest))
                    .isInstanceOf(JwtAuthException.class)
                    .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

    }

    @Nested
    @DisplayName("회원 탈퇴")
    class BlindMemberTest {

        @DisplayName("성공: 회원의 blind가 true로 변경 되어야 한다.")
        @Test
        void blindMemberSucceedsBlindTrue() {
            // given

            // when
            authFacadeService.blindMember(member);

            // then
            assertThat(member.getBlind()).isTrue();
        }

        @DisplayName("성공: 해당 회원이 속한 모든 채팅방에서 퇴장 처리 되어야 한다.")
        @Test
        void blindMemberSucceedsExitChatrooms() {
            // given

            // when

            // then
        }

        @DisplayName("성공: 해당 회원이 보낸 모든 친구 요청이 취소 처리 되어야 한다.")
        @Test
        void blindMemberSucceedsCancelSendFriendRequests() {
            // given

            // when

            // then
        }

        @DisplayName("성공: 해당 회원이 받은 모든 친구 요청이 취소 처리 되어야 한다.")
        @Test
        void blindMemberSucceedsCancelReceivedFriendRequests() {
            // given

            // when

            // then
        }

        @DisplayName("성공: 해당 회원이 작성한 모든 게시글이 삭제 처리 되어야 한다.")
        @Test
        void blindMemberSucceedsDeletePosts() {
            // given

            // when

            // then
        }

        @DisplayName("성공: 해당 회원이 남긴 모든 매너평가가 삭제 처리 되어야 한다.")
        @Test
        void blindMemberSucceedsDeleteMannerRatings() {
            // given

            // when

            // then
        }

        @DisplayName("성공: 해당 회원의 refresh token이 삭제 되어야 한다.")
        @Test
        void blindMemberSucceedsDeleteRefreshToken() {
            // given

            // when

            // then
        }

    }

    private Member createMember(String email, String gameName, String password) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password(password)
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .soloTier(Tier.IRON)
                .soloRank(0)
                .soloWinRate(0.0)
                .soloGameCount(0)
                .freeTier(Tier.IRON)
                .freeRank(0)
                .freeWinRate(0.0)
                .freeGameCount(0)
                .isAgree(true)
                .build());
    }

    private static void checkExpirationTime(Long tokenExpirationTime) {
        long currentTimeMillis = Instant.now().toEpochMilli(); // 현재 시간

        // AssertJ를 이용해 만료 시간이 현재 시간 이후인지 확인
        assertThat(tokenExpirationTime)
                .as("Check if the token expiration time is after the current time")
                .isGreaterThan(currentTimeMillis);
    }

}
