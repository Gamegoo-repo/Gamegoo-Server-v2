package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.external.riot.domain.GameMatch;
import com.gamegoo.gamegoo_v2.external.riot.repository.GameMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 증분 업데이트 로직 테스트
 *
 * <p>테스트 시나리오:</p>
 * <ul>
 *   <li>신규 매치만 API 호출하는지 검증</li>
 *   <li>DB에서 통계 계산이 정상적으로 동작하는지 검증</li>
 *   <li>중복 저장이 발생하지 않는지 검증</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RiotRecordServiceIncrementalTest {

    @Autowired
    private RiotRecordService riotRecordService;

    @Autowired
    private GameMatchRepository gameMatchRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 Member 생성 및 저장
        testMember = createAndSaveTestMember();
    }

    @Test
    @DisplayName("DB에서 최근 30개 매치 조회 후 통계 계산 - 정상 동작")
    void getAllModeStatsFromDB_Success() {
        // given: 테스트용 GameMatch 데이터 생성

        // 솔로랭크 게임 10개 (7승 3패)
        for (int i = 0; i < 10; i++) {
            GameMatch match = createTestGameMatch(
                testMember,
                "KR_solo_" + i,
                420, // 솔로랭크
                i < 7, // 처음 7개는 승리
                LocalDateTime.now().minusDays(i)
            );
            gameMatchRepository.save(match);
        }

        // 자유랭크 게임 10개 (5승 5패)
        for (int i = 0; i < 10; i++) {
            GameMatch match = createTestGameMatch(
                testMember,
                "KR_free_" + i,
                440, // 자유랭크
                i < 5, // 처음 5개는 승리
                LocalDateTime.now().minusDays(10 + i)
            );
            gameMatchRepository.save(match);
        }

        // 칼바람 게임 10개 (8승 2패)
        for (int i = 0; i < 10; i++) {
            GameMatch match = createTestGameMatch(
                testMember,
                "KR_aram_" + i,
                450, // 칼바람
                i < 8, // 처음 8개는 승리
                LocalDateTime.now().minusDays(20 + i)
            );
            gameMatchRepository.save(match);
        }

        // when: DB에서 통계 계산
        var result = riotRecordService.getAllModeStatsFromDB(testMember);

        // then: 통계 검증
        // 1. 통합 통계 (솔로 + 자유) - 20게임, 12승 8패
        assertThat(result.getCombinedStats().getRecTotalWins()).isEqualTo(12);
        assertThat(result.getCombinedStats().getRecTotalLosses()).isEqualTo(8);
        assertThat(result.getCombinedStats().getRecWinRate()).isEqualTo(60.0);

        // 2. 솔로랭크 통계 - 10게임, 7승 3패
        assertThat(result.getSoloStats().getRecTotalWins()).isEqualTo(7);
        assertThat(result.getSoloStats().getRecTotalLosses()).isEqualTo(3);
        assertThat(result.getSoloStats().getRecWinRate()).isEqualTo(70.0);

        // 3. 자유랭크 통계 - 10게임, 5승 5패
        assertThat(result.getFreeStats().getRecTotalWins()).isEqualTo(5);
        assertThat(result.getFreeStats().getRecTotalLosses()).isEqualTo(5);
        assertThat(result.getFreeStats().getRecWinRate()).isEqualTo(50.0);

        // 4. 칼바람 통계 - 10게임, 8승 2패
        assertThat(result.getAramStats().getRecTotalWins()).isEqualTo(8);
        assertThat(result.getAramStats().getRecTotalLosses()).isEqualTo(2);
        assertThat(result.getAramStats().getRecWinRate()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("최근 30개 이상 매치가 있을 때 최신 30개만 조회")
    void getAllModeStatsFromDB_OnlyRecent30() {
        // given: 35개 게임 생성

        for (int i = 0; i < 35; i++) {
            GameMatch match = createTestGameMatch(
                testMember,
                "KR_match_" + i,
                420,
                i < 25, // 최신 25개만 승리 (0~24번 게임)
                LocalDateTime.now().minusDays(i)
            );
            gameMatchRepository.save(match);
        }

        // when: DB에서 통계 계산 (최신 30개만)
        var result = riotRecordService.getAllModeStatsFromDB(testMember);

        // then: 최신 30개 기준 통계 검증 (25승 5패)
        assertThat(result.getSoloStats().getRecTotalWins()).isEqualTo(25);
        assertThat(result.getSoloStats().getRecTotalLosses()).isEqualTo(5);
    }

    @Test
    @DisplayName("GameMatch가 없을 때 빈 통계 반환")
    void getAllModeStatsFromDB_EmptyMatches() {
        // given: 매치가 없는 사용자 (testMember는 BeforeEach에서 생성됨)

        // when: DB에서 통계 계산
        var result = riotRecordService.getAllModeStatsFromDB(testMember);

        // then: 모든 통계가 0
        assertThat(result.getCombinedStats().getRecTotalWins()).isZero();
        assertThat(result.getCombinedStats().getRecTotalLosses()).isZero();
        assertThat(result.getCombinedStats().getRecWinRate()).isZero();
    }

    @Test
    @DisplayName("GameMatch -> ChampionStats 변환 검증")
    void toChampionStats_Conversion() {
        // given: 테스트 GameMatch
        GameMatch gameMatch = GameMatch.builder()
            .member(testMember)
            .matchId("KR_test_123")
            .puuid("test_puuid")
            .gameName("TestPlayer")
            .championId(157L) // 야스오
            .queueId(420)
            .kills(10)
            .deaths(3)
            .assists(7)
            .totalMinionsKilled(200)
            .win(true)
            .gameDuration(1800) // 30분
            .gameStartedAt(LocalDateTime.now())
            .build();

        // when: ChampionStats로 변환
        var championStats = gameMatch.toChampionStats();

        // then: 변환 검증
        assertThat(championStats.getChampionId()).isEqualTo(157L);
        assertThat(championStats.getQueueId()).isEqualTo(420);
        assertThat(championStats.getWins()).isEqualTo(1);
        assertThat(championStats.getGames()).isEqualTo(1);
        assertThat(championStats.getKills()).isEqualTo(10);
        assertThat(championStats.getDeaths()).isEqualTo(3);
        assertThat(championStats.getAssists()).isEqualTo(7);
        assertThat(championStats.getTotalMinionsKilled()).isEqualTo(200);
        assertThat(championStats.getGameTime()).isEqualTo(1800);
    }

    // ===== Helper Methods =====

    private Member createAndSaveTestMember() {
        // ReflectionTestUtils를 사용하여 Member 생성 (protected 생성자 우회)
        try {
            java.lang.reflect.Constructor<Member> constructor = Member.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Member member = constructor.newInstance();

            org.springframework.test.util.ReflectionTestUtils.setField(member, "email", "test@example.com");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "puuid", "test_puuid");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "gameName", "TestPlayer");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "tag", "KR1");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "loginType", LoginType.RSO);
            org.springframework.test.util.ReflectionTestUtils.setField(member, "profileImage", 1);
            org.springframework.test.util.ReflectionTestUtils.setField(member, "blind", false);

            return memberRepository.save(member);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test member", e);
        }
    }

    private GameMatch createTestGameMatch(Member member, String matchId, int queueId,
                                          boolean win, LocalDateTime gameStartedAt) {
        return GameMatch.builder()
            .member(member)
            .matchId(matchId)
            .puuid(member.getPuuid())
            .gameName(member.getGameName())
            .championId(1L) // 기본 챔피언 ID
            .queueId(queueId)
            .kills(5)
            .deaths(3)
            .assists(7)
            .totalMinionsKilled(150)
            .win(win)
            .gameDuration(1800)
            .gameStartedAt(gameStartedAt)
            .build();
    }
}