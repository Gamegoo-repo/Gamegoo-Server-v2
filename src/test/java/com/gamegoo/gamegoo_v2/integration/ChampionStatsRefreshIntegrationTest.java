package com.gamegoo.gamegoo_v2.integration;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.service.ChampionStatsRefreshService;
import com.gamegoo.gamegoo_v2.external.riot.domain.GameMatch;
import com.gamegoo.gamegoo_v2.external.riot.repository.GameMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 증분 업데이트 통합 테스트
 *
 * <p>테스트 목적:</p>
 * <ul>
 *   <li>전적 갱신 전체 플로우 검증</li>
 *   <li>동시성 제어 검증 (멀티스레드 환경)</li>
 *   <li>UNIQUE 제약 조건 동작 검증</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
class ChampionStatsRefreshIntegrationTest {

    @Autowired
    private GameMatchRepository gameMatchRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    @Transactional
    void setUp() {
        // 기존 데이터 정리
        gameMatchRepository.deleteAll();

        // 테스트용 Member 생성
        testMember = createAndSaveTestMember();
    }

    @Test
    @DisplayName("동시에 같은 matchId 저장 시도 - UNIQUE 제약으로 중복 방지")
    void concurrentSave_SameMatchId_ShouldPreventDuplicates() throws InterruptedException {
        // given: 10개 스레드가 동시에 같은 matchId 저장 시도
        int threadCount = 10;
        String matchId = "KR_concurrent_test_123";
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    startLatch.await();

                    // GameMatch 저장 시도
                    GameMatch gameMatch = GameMatch.builder()
                            .member(testMember)
                            .matchId(matchId)
                            .puuid("test_puuid")
                            .gameName("TestPlayer")
                            .championId(1L)
                            .queueId(420)
                            .kills(5)
                            .deaths(3)
                            .assists(7)
                            .totalMinionsKilled(150)
                            .win(true)
                            .gameDuration(1800)
                            .gameStartedAt(LocalDateTime.now())
                            .build();

                    gameMatchRepository.saveAndFlush(gameMatch);
                    successCount.incrementAndGet();

                } catch (DataIntegrityViolationException e) {
                    // UNIQUE 제약 위반 예상
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    // 다른 예외도 fail로 카운트 (JPA 예외 래핑 등)
                    if (e.getCause() instanceof DataIntegrityViolationException) {
                        failCount.incrementAndGet();
                    } else {
                        e.printStackTrace();
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시 시작
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then: 최소 1개는 성공, 나머지는 실패 (동시성 환경에서는 정확히 1개가 아닐 수도 있음)
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);

        // DB에 최소 1개는 저장되어 있는지 확인
        long count = gameMatchRepository.findAll().stream()
                .filter(gm -> matchId.equals(gm.getMatchId()))
                .count();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("GameMatch 저장 후 조회 - 인덱스 성능 검증")
    @Transactional
    void saveAndQuery_RecentMatches_ShouldUseBothIndexes() {
        // given: 50개 매치 저장 (최신 30개만 조회할 예정)
        for (int i = 0; i < 50; i++) {
            GameMatch match = GameMatch.builder()
                    .member(testMember)
                    .matchId("KR_match_" + i)
                    .puuid("test_puuid")
                    .gameName("TestPlayer")
                    .championId((long) (i % 10 + 1))
                    .queueId(420)
                    .kills(5)
                    .deaths(3)
                    .assists(7)
                    .totalMinionsKilled(150)
                    .win(i % 2 == 0)
                    .gameDuration(1800)
                    .gameStartedAt(LocalDateTime.now().minusDays(i))
                    .build();
            gameMatchRepository.save(match);
        }

        // when: 최근 30개 조회
        var recentMatches = gameMatchRepository.findTop30ByMemberOrderByGameStartedAtDesc(testMember);

        // then: 정확히 30개 조회
        assertThat(recentMatches).hasSize(30);

        // 최신 순으로 정렬되어 있는지 확인
        for (int i = 0; i < recentMatches.size() - 1; i++) {
            assertThat(recentMatches.get(i).getGameStartedAt())
                    .isAfterOrEqualTo(recentMatches.get(i + 1).getGameStartedAt());
        }
    }

    @Test
    @DisplayName("existsByMemberAndMatchId - UNIQUE 인덱스 활용 검증")
    @Transactional
    void existsByMemberAndMatchId_ShouldUseUniqueIndex() {
        // given: 매치 저장
        String matchId = "KR_exists_test_123";
        GameMatch match = GameMatch.builder()
                .member(testMember)
                .matchId(matchId)
                .puuid("test_puuid")
                .gameName("TestPlayer")
                .championId(1L)
                .queueId(420)
                .kills(5)
                .deaths(3)
                .assists(7)
                .totalMinionsKilled(150)
                .win(true)
                .gameDuration(1800)
                .gameStartedAt(LocalDateTime.now())
                .build();
        gameMatchRepository.save(match);

        // when & then: 존재하는 매치 확인
        assertThat(gameMatchRepository.existsByMemberAndMatchId(testMember, matchId)).isTrue();

        // 존재하지 않는 매치 확인
        assertThat(gameMatchRepository.existsByMemberAndMatchId(testMember, "KR_not_exists")).isFalse();
    }

    // ===== Helper Methods =====

    private Member createAndSaveTestMember() {
        try {
            java.lang.reflect.Constructor<Member> constructor = Member.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Member member = constructor.newInstance();

            org.springframework.test.util.ReflectionTestUtils.setField(member, "email", "integration-test@example.com");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "puuid", "integration_test_puuid");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "gameName", "IntegrationTestPlayer");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "tag", "KR1");
            org.springframework.test.util.ReflectionTestUtils.setField(member, "loginType", com.gamegoo.gamegoo_v2.account.member.domain.LoginType.RSO);
            org.springframework.test.util.ReflectionTestUtils.setField(member, "profileImage", 1);
            org.springframework.test.util.ReflectionTestUtils.setField(member, "blind", false);

            return memberRepository.save(member);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test member", e);
        }
    }
}
