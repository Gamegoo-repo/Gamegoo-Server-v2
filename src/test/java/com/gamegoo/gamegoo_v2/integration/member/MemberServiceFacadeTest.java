package com.gamegoo.gamegoo_v2.integration.member;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberChampion;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.dto.request.GameStyleRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.IsMikeRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.PositionRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.ProfileImageRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MyProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.dto.response.OtherProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberGameStyleRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.service.MemberFacadeService;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import com.gamegoo.gamegoo_v2.game.repository.ChampionRepository;
import com.gamegoo.gamegoo_v2.game.repository.GameStyleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceFacadeTest {

    @Autowired
    MemberFacadeService memberFacadeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChampionRepository championRepository;

    @Autowired
    MemberChampionRepository memberChampionRepository;

    @Autowired
    GameStyleRepository gameStyleRepository;

    @Autowired
    MemberGameStyleRepository memberGameStyleRepository;

    private static Member member;
    private static Member targetMember;

    private static Champion annie;
    private static Champion olaf;
    private static Champion galio;


    @BeforeEach
    void setUp() {
        // Member 테스트용 객체 생성
        member = createMember("test1@gmail.com", "test1");
        targetMember = createMember("test2@gmail.com", "test2");

        // Champion 테스트용 객체 생성
        annie = initChampion(1L, "Annie");
        olaf = initChampion(2L, "Olaf");
        galio = initChampion(3L, "Galio");

        List<Long> championIds = Arrays.asList(annie.getId(), olaf.getId(), galio.getId());
        initMemberChampion(member, championIds);
        initMemberChampion(targetMember, championIds);

        // 게임 스타일 저장
        for (long i = 1; i <= 16; i++) {
            gameStyleRepository.save(GameStyle.create("StyleName" + i));
        }
    }

    @AfterEach
    void tearDown() {
        memberChampionRepository.deleteAllInBatch();
        championRepository.deleteAllInBatch();
        memberGameStyleRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("내 프로필 조회 성공")
    @Test
    void getProfile() {
        // when
        MyProfileResponse response = memberFacadeService.getMyProfile(member);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(member.getId());
        assertThat(response.getProfileImg()).isEqualTo(member.getProfileImage());
        assertThat(response.getMike()).isEqualTo(member.getMike());
        assertThat(response.getEmail()).isEqualTo(member.getEmail());
        assertThat(response.getGameName()).isEqualTo(member.getGameName());
        assertThat(response.getTag()).isEqualTo(member.getTag());
        assertThat(response.getSoloTier()).isEqualTo(member.getSoloTier());
        assertThat(response.getSoloRank()).isEqualTo(member.getSoloRank());
        assertThat(response.getSoloWinrate()).isEqualTo(member.getSoloWinRate());
        assertThat(response.getFreeTier()).isEqualTo(member.getFreeTier());
        assertThat(response.getFreeRank()).isEqualTo(member.getFreeRank());
        assertThat(response.getFreeWinrate()).isEqualTo(member.getFreeWinRate());
        assertThat(response.getMainP()).isEqualTo(member.getMainP());
        assertThat(response.getSubP()).isEqualTo(member.getSubP());
        assertThat(response.getWantP()).isEqualTo(member.getWantP());
        assertThat(response.getIsAgree()).isEqualTo(member.isAgree());
        assertThat(response.getIsBlind()).isEqualTo(member.isBlind());
        assertThat(response.getLoginType()).isEqualTo(member.getLoginType().name());
        assertThat(response.getChampionResponseList()).isNotNull();

        List<Champion> championList =
                targetMember.getMemberChampionList().stream().map(MemberChampion::getChampion).toList();
        List<Long> championIds = championList.stream().map(Champion::getId).toList();

        for (int i = 0; i < championIds.size(); i++) {
            assertThat(response.getChampionResponseList().get(i).getChampionId()).isEqualTo(championIds.get(i));
        }
    }

    @DisplayName("다른 사람 프로필 조회 성공")
    @Test
    void getOtherProfile() {
        // when
        OtherProfileResponse response = memberFacadeService.getOtherProfile(member, targetMember.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(targetMember.getId());
        assertThat(response.getProfileImg()).isEqualTo(targetMember.getProfileImage());
        assertThat(response.getMike()).isEqualTo(targetMember.getMike());
        assertThat(response.getGameName()).isEqualTo(targetMember.getGameName());
        assertThat(response.getTag()).isEqualTo(targetMember.getTag());
        assertThat(response.getSoloTier()).isEqualTo(targetMember.getSoloTier());
        assertThat(response.getSoloRank()).isEqualTo(targetMember.getSoloRank());
        assertThat(response.getSoloWinrate()).isEqualTo(targetMember.getSoloWinRate());
        assertThat(response.getFreeTier()).isEqualTo(targetMember.getFreeTier());
        assertThat(response.getFreeRank()).isEqualTo(targetMember.getFreeRank());
        assertThat(response.getFreeWinrate()).isEqualTo(targetMember.getFreeWinRate());
        assertThat(response.getMainP()).isEqualTo(targetMember.getMainP());
        assertThat(response.getSubP()).isEqualTo(targetMember.getSubP());
        assertThat(response.getWantP()).isEqualTo(targetMember.getWantP());
        assertThat(response.getIsAgree()).isEqualTo(targetMember.isAgree());
        assertThat(response.getIsBlind()).isEqualTo(targetMember.isBlind());
        assertThat(response.getLoginType()).isEqualTo(String.valueOf(targetMember.getLoginType()));
        assertThat(response.getChampionResponseList()).isNotNull();

        List<Champion> championList =
                targetMember.getMemberChampionList().stream().map(MemberChampion::getChampion).toList();
        List<Long> championIds = championList.stream().map(Champion::getId).toList();

        for (int i = 0; i < championIds.size(); i++) {
            assertThat(response.getChampionResponseList().get(i).getChampionId()).isEqualTo(championIds.get(i));
        }
    }

    @DisplayName("프로필 이미지 변경 성공")
    @Test
    void setProfileImage() {
        // given
        ProfileImageRequest request = ProfileImageRequest.builder()
                .profileImage(2)
                .build();
        // when
        memberFacadeService.setProfileImage(member, request);
        // then
        assertThat(member.getProfileImage()).isEqualTo(request.getProfileImage());
    }

    @DisplayName("마이크 유무 변경 성공")
    @Test
    void setMike() {
        // given
        IsMikeRequest request = IsMikeRequest.builder()
                .mike(Mike.AVAILABLE)
                .build();
        // when
        memberFacadeService.setMike(member, request);
        // then
        assertThat(member.getMike()).isEqualTo(request.getMike());
    }

    @DisplayName("주/부/원하는 포지션 변경 성공")
    @Test
    void setPosition() {
        // given
        PositionRequest request = PositionRequest.builder()
                .mainP(Position.valueOf("TOP"))
                .subP(Position.valueOf("ANY"))
                .wantP(Position.valueOf("MID"))
                .build();
        // when
        memberFacadeService.setPosition(member, request);
        // then
        assertThat(member.getMainP()).isEqualTo(request.getMainP());
        assertThat(member.getSubP()).isEqualTo(request.getSubP());
        assertThat(member.getWantP()).isEqualTo(request.getWantP());
    }

    @Nested
    @DisplayName("게임스타일 수정 테스트")
    class GameStyleUpdateTest {

        @DisplayName("게임 스타일 수정 성공: 게임스타일 개수가 3개일 경우")
        @Test
        void updateGameStyleWith3Ids() {
            // given
            List<Long> randomGameStyleIds = generateRandomGameStyleIds(3);
            GameStyleRequest request = GameStyleRequest.builder()
                    .gameStyleIdList(randomGameStyleIds)
                    .build();

            // when
            String response = memberFacadeService.setGameStyle(member, request);

            // then
            assertThat(response).isEqualTo("게임 스타일 수정이 완료되었습니다");
            assertGameStylesMatch(randomGameStyleIds);
        }

        @DisplayName("게임 스타일 수정 성공: 게임스타일 개수가 2개일 경우")
        @Test
        void updateGameStyleWith2Ids() {
            // given
            List<Long> randomGameStyleIds = generateRandomGameStyleIds(2);
            GameStyleRequest request = GameStyleRequest.builder()
                    .gameStyleIdList(randomGameStyleIds)
                    .build();

            // when
            String response = memberFacadeService.setGameStyle(member, request);

            // then
            assertThat(response).isEqualTo("게임 스타일 수정이 완료되었습니다");
            assertGameStylesMatch(randomGameStyleIds);
        }

        @DisplayName("게임 스타일 수정 성공: 게임스타일 개수가 1개일 경우")
        @Test
        void updateGameStyleWith1Id() {
            // given
            List<Long> randomGameStyleIds = generateRandomGameStyleIds(1);
            GameStyleRequest request = GameStyleRequest.builder()
                    .gameStyleIdList(randomGameStyleIds)
                    .build();

            // when
            String response = memberFacadeService.setGameStyle(member, request);

            // then
            assertThat(response).isEqualTo("게임 스타일 수정이 완료되었습니다");
            assertGameStylesMatch(randomGameStyleIds);
        }

        @DisplayName("게임 스타일 수정 성공: 게임스타일 개수가 0개일 경우")
        @Test
        void updateGameStyleWith0Id() {
            // given
            GameStyleRequest request = GameStyleRequest.builder()
                    .gameStyleIdList(Collections.emptyList())
                    .build();

            // when
            String response = memberFacadeService.setGameStyle(member, request);

            // then
            assertThat(response).isEqualTo("게임 스타일 수정이 완료되었습니다");
            assertGameStylesMatch(Collections.emptyList());
        }

        private void assertGameStylesMatch(List<Long> expectedGameStyleIds) {
            // 현재 MemberGameStyle 리스트 추출
            List<Long> actualGameStyleIds = member.getMemberGameStyleList().stream()
                    .map(memberGameStyle -> memberGameStyle.getGameStyle().getId())
                    .toList();

            // expectedGameStyleIds가 비어 있을 경우 MemberGameStyle도 비어 있는지 검증
            if (expectedGameStyleIds.isEmpty()) {
                assertThat(actualGameStyleIds)
                        .describedAs("Expected no game styles, but found: %s", actualGameStyleIds)
                        .isEmpty();
            } else {
                // 일반적인 검증 로직
                assertThat(actualGameStyleIds)
                        .containsExactlyInAnyOrderElementsOf(expectedGameStyleIds)
                        .withFailMessage("Expected game style IDs: %s but found: %s", expectedGameStyleIds,
                                actualGameStyleIds);
            }
        }


        private List<Long> generateRandomGameStyleIds(int count) {
            return ThreadLocalRandom.current()
                    .longs(1, 17) // Generate random numbers between 1 and 16 (inclusive)
                    .distinct()
                    .limit(count)
                    .boxed()
                    .toList();
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

    private Champion initChampion(Long id, String name) {
        Champion champion = Champion.create(id, name);
        championRepository.save(champion);
        return champion;
    }

    private void initMemberChampion(Member member, List<Long> top3ChampionIds) {
        top3ChampionIds.forEach(championId -> {
            Champion champion = championRepository.findById(championId).isPresent() ?
                    championRepository.findById(championId).get() : null;
            MemberChampion memberChampion = MemberChampion.create(champion, member, 1, 10);
            memberChampionRepository.save(memberChampion);
        });

    }

}
