package com.gamegoo.gamegoo_v2.integration.manner;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.social.manner.repository.MannerKeywordRepository;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerFacadeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class MannerFacadeServiceTest {

    @Autowired
    MannerFacadeService mannerFacadeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MannerKeywordRepository mannerKeywordRepository;

    private static final String TARGET_EMAIL = "target@naver.com";
    private static final String TARGET_GAMENAME = "target";

    private Member member;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("매너 평가 등록")
    class InsertPositiveMannerRatingTest {

        @DisplayName("실패: 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMemberNotFound() {
            // given

            // when

            // then
        }

        @DisplayName("실패: 매너 키워드 id에 7 이상의 값이 들어간 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMannerKeywordIsNotValid() {
            // given

            // when

            // then
        }

        @DisplayName("실패: 매너 키워드 id에 음수 값이 들어간 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMannerKeywordIsNegative() {
            // given

            // when

            // then
        }

        @DisplayName("실패: 평가 대상으로 본인 id를 입력한 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenTargetIsSelf() {
            // given

            // when

            // then
        }

        @DisplayName("실패: 대상 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenTargetIsBlind() {
            // given

            // when

            // then
        }

        @DisplayName("실패: 대상 회원에게 등록한 매너 평가가 존재하는 경우 예외가 발생한다.")
        @Test
        void insertPositiveMannerRating_shouldThrownWhenMannerRatingExists() {
            // given

            // when

            // then
        }

        @DisplayName("성공: 엔티티 생성 및 저장, 매너 평가 등록 알림이 생성되어야 한다.")
        @Test
        void insertPositiveMannerRatingSucceeds() {
            // given

            // when

            // then
        }

    }


    private Member createMember(String email, String gameName) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .tier(Tier.IRON)
                .gameRank(0)
                .winRate(0.0)
                .gameCount(0)
                .isAgree(true)
                .build());
    }

}
