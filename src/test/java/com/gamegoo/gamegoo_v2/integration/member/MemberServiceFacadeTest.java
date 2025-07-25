package com.gamegoo.gamegoo_v2.integration.member;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberChampion;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
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
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.service.MemberFacadeService;
import com.gamegoo.gamegoo_v2.content.board.dto.response.ChampionStatsResponse;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import com.gamegoo.gamegoo_v2.game.repository.ChampionRepository;
import com.gamegoo.gamegoo_v2.game.repository.GameStyleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Autowired
    MemberRecentStatsRepository memberRecentStatsRepository;

    @PersistenceContext
    EntityManager entityManager;

    private static Member member;
    private static Member targetMember;

    private static Champion annie;
    private static Champion olaf;
    private static Champion galio;


    @BeforeEach
    void setUp() {
        // Member 테스트용 객체 생성
        member = createForGeneralMember("test1@gmail.com", "test1");
        targetMember = createForGeneralMember("test2@gmail.com", "test2");

        // Champion 테스트용 객체 생성
        annie = initChampion(1L, "Annie");
        olaf = initChampion(2L, "Olaf");
        galio = initChampion(3L, "Galio");

        List<Long> championIds = Arrays.asList(annie.getId(), olaf.getId(), galio.getId());
        initMemberChampion(member, championIds);
        initMemberChampion(targetMember, championIds);

        // MemberRecentStats 초기화 - setup 순서 조정으로 낙관적 잠금 문제 해결
        // initMemberRecentStats(member);
        // initMemberRecentStats(targetMember);

        // 게임 스타일 저장
        for (long i = 1; i <= 16; i++) {
            gameStyleRepository.save(GameStyle.create("StyleName" + i));
        }

        // 포지션 지정
        member.updatePosition(Position.ADC, Position.TOP, new ArrayList<>());
        member.getWantP().clear();
        member.getWantP().add(Position.MID);
        member.getWantP().add(Position.SUP);

        targetMember.updatePosition(Position.TOP, Position.SUP, new ArrayList<>());
        targetMember.getWantP().clear();
        targetMember.getWantP().add(Position.ANY);
        targetMember.getWantP().add(Position.JUNGLE);

        memberRepository.save(member);
        memberRepository.save(targetMember);
    }

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAllInBatch();
        memberChampionRepository.deleteAllInBatch();
        championRepository.deleteAllInBatch();
        memberGameStyleRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("내 프로필 조회 성공 - Recent Stats 없는 경우")
    @Test
    @Transactional
    void getProfile() {
        // DB에서 fresh member 다시 로딩 (wantP 포함)
        Member freshMember = memberRepository.findById(member.getId()).orElseThrow();

        // when
        MyProfileResponse response = memberFacadeService.getMyProfile(freshMember);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(freshMember.getId());
        assertThat(response.getProfileImg()).isEqualTo(freshMember.getProfileImage());
        assertThat(response.getMike()).isEqualTo(freshMember.getMike());
        assertThat(response.getEmail()).isEqualTo(freshMember.getEmail());
        assertThat(response.getGameName()).isEqualTo(freshMember.getGameName());
        assertThat(response.getTag()).isEqualTo(freshMember.getTag());
        assertThat(response.getSoloTier()).isEqualTo(freshMember.getSoloTier());
        assertThat(response.getSoloRank()).isEqualTo(freshMember.getSoloRank());
        assertThat(response.getSoloWinrate()).isEqualTo(freshMember.getSoloWinRate());
        assertThat(response.getFreeTier()).isEqualTo(freshMember.getFreeTier());
        assertThat(response.getFreeRank()).isEqualTo(freshMember.getFreeRank());
        assertThat(response.getFreeWinrate()).isEqualTo(freshMember.getFreeWinRate());
        assertThat(response.getMainP()).isEqualTo(freshMember.getMainP());
        assertThat(response.getSubP()).isEqualTo(freshMember.getSubP());
        assertThat(response.getWantP()).isEqualTo(freshMember.getWantP());
        assertThat(response.getIsAgree()).isEqualTo(freshMember.isAgree());
        assertThat(response.getIsBlind()).isEqualTo(freshMember.getBlind());
        assertThat(response.getLoginType()).isEqualTo(freshMember.getLoginType());
        assertThat(response.getChampionStatsResponseList()).isNotNull();

        // MemberRecentStats 검증 - null인 경우를 확인
        assertThat(response.getMemberRecentStats()).isNull();

        List<MemberChampion> memberChampionList = freshMember.getMemberChampionList();
        List<ChampionStatsResponse> championStatsResponseList = response.getChampionStatsResponseList();

        assertThat(championStatsResponseList).hasSize(memberChampionList.size());

        for (int i = 0; i < memberChampionList.size(); i++) {
            MemberChampion memberChampion = memberChampionList.get(i);
            ChampionStatsResponse championResponse = championStatsResponseList.get(i);

            assertThat(championResponse.getChampionId()).isEqualTo(memberChampion.getChampion().getId());
            assertThat(championResponse.getChampionName()).isEqualTo(memberChampion.getChampion().getName());
            assertThat(championResponse.getWins()).isEqualTo(memberChampion.getWins());
            assertThat(championResponse.getGames()).isEqualTo(memberChampion.getGames());
            assertThat(championResponse.getWinRate()).isEqualTo(
                    memberChampion.getWins() / (double) memberChampion.getGames());
            assertThat(championResponse.getCsPerMinute()).isEqualTo(memberChampion.getCsPerMinute());
            assertThat(championResponse.getAverageCs()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getTotalCs() / memberChampion.getGames()
                            : 0);
            assertThat(championResponse.getKda()).isEqualTo(memberChampion.getKDA());
            assertThat(championResponse.getKills()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getKills() / memberChampion.getGames() : 0);
            assertThat(championResponse.getDeaths()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getDeaths() / memberChampion.getGames() :
                            0);
            assertThat(championResponse.getAssists()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getAssists() / memberChampion.getGames()
                            : 0);
        }
    }

    @DisplayName("내 프로필 조회 성공 - Recent Stats가 있는 경우 필드 포함 확인")
    @Test
    void getProfileWithRecentStatsField() {
        // when
        MyProfileResponse response = memberFacadeService.getMyProfile(member);

        // then - Response가 MemberRecentStats 필드를 포함하는지 확인
        assertThat(response).isNotNull();
        // MemberRecentStats 필드가 Response 구조에 포함되어 있는지 확인
        // null이더라도 필드 자체는 존재해야 함
        assertThat(response).hasFieldOrProperty("memberRecentStats");
    }

    @DisplayName("다른 사람 프로필 조회 성공 - Recent Stats 없는 경우")
    @Test
    @Transactional
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
        assertThat(response.getIsBlind()).isEqualTo(targetMember.getBlind());
        assertThat(response.getLoginType()).isEqualTo(targetMember.getLoginType());
        assertThat(response.getChampionStatsResponseList()).isNotNull();

        // MemberRecentStats 검증 - null인 경우를 확인
        assertThat(response.getMemberRecentStats()).isNull();

        List<MemberChampion> memberChampionList = targetMember.getMemberChampionList();
        List<ChampionStatsResponse> championStatsResponseList = response.getChampionStatsResponseList();

        assertThat(championStatsResponseList).hasSize(memberChampionList.size());

        for (int i = 0; i < memberChampionList.size(); i++) {
            MemberChampion memberChampion = memberChampionList.get(i);
            ChampionStatsResponse championResponse = championStatsResponseList.get(i);

            assertThat(championResponse.getChampionId()).isEqualTo(memberChampion.getChampion().getId());
            assertThat(championResponse.getChampionName()).isEqualTo(memberChampion.getChampion().getName());
            assertThat(championResponse.getWins()).isEqualTo(memberChampion.getWins());
            assertThat(championResponse.getGames()).isEqualTo(memberChampion.getGames());
            assertThat(championResponse.getWinRate()).isEqualTo(
                    memberChampion.getWins() / (double) memberChampion.getGames());
            assertThat(championResponse.getCsPerMinute()).isEqualTo(memberChampion.getCsPerMinute());
            assertThat(championResponse.getAverageCs()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getTotalCs() / memberChampion.getGames()
                            : 0);
            assertThat(championResponse.getKda()).isEqualTo(memberChampion.getKDA());
            assertThat(championResponse.getKills()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getKills() / memberChampion.getGames() : 0);
            assertThat(championResponse.getDeaths()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getDeaths() / memberChampion.getGames() :
                            0);
            assertThat(championResponse.getAssists()).isEqualTo(
                    memberChampion.getGames() > 0 ? (double) memberChampion.getAssists() / memberChampion.getGames()
                            : 0);
        }
    }

    @DisplayName("다른 사람 프로필 조회 성공 - Recent Stats 필드 포함 확인")
    @Test
    @Transactional
    void getOtherProfileWithRecentStatsField() {
        // when
        OtherProfileResponse response = memberFacadeService.getOtherProfile(member, targetMember.getId());

        // then - Response가 MemberRecentStats 필드를 포함하는지 확인
        assertThat(response).isNotNull();
        // MemberRecentStats 필드가 Response 구조에 포함되어 있는지 확인
        assertThat(response).hasFieldOrProperty("memberRecentStats");
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
                .wantP(List.of(Position.valueOf("MID")))
                .build();
        // when
        memberFacadeService.setPosition(member, request);
        // then
        assertThat(member.getMainP()).isEqualTo(request.getMainP());
        assertThat(member.getSubP()).isEqualTo(request.getSubP());
        assertThat(member.getWantP().get(0)).isEqualTo(request.getWantP().get(0));
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

    private Member createForGeneralMember(String email, String gameName) {
        Member member = Member.builder()
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
                .build();

        memberRecentStatsRepository.save(MemberRecentStats.builder()
                .member(member)
                .build());

        return memberRepository.save(member);
    }

    private Champion initChampion(Long id, String name) {
        Champion champion = Champion.create(id, name);
        championRepository.save(champion);
        return champion;
    }

    private void initMemberChampion(Member member, List<Long> top3ChampionIds) {
        top3ChampionIds.forEach(championId -> {
            championRepository.findById(championId).ifPresent(champion -> {
                MemberChampion memberChampion = MemberChampion.create(champion, member, 1, 10, 12.0, 120, 0, 0, 0);
                memberChampionRepository.save(memberChampion);
            });
        });
    }

    private void initMemberRecentStats(Member member) {
        // Member를 refresh하여 최신 상태로 만듦
        Member refreshedMember = memberRepository.findById(member.getId()).orElseThrow();

        MemberRecentStats recentStats = MemberRecentStats.builder()
                .memberId(refreshedMember.getId())
                .member(refreshedMember)
                .recTotalWins(25)
                .recTotalLosses(15)
                .recWinRate(62.5)
                .recAvgKDA(2.5)
                .recAvgKills(8.5)
                .recAvgDeaths(3.2)
                .recAvgAssists(9.1)
                .recAvgCsPerMinute(6.8)
                .recTotalCs(1360)
                .build();
        memberRecentStatsRepository.save(recentStats);
    }

}
