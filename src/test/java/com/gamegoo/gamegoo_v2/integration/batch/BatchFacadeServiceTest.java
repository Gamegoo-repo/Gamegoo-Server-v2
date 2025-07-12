package com.gamegoo.gamegoo_v2.integration.batch;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.batch.BatchFacadeService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
class BatchFacadeServiceTest {

    @Autowired
    private BatchFacadeService batchFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberRecentStatsRepository memberRecentStatsRepository;

    @MockitoSpyBean
    private EntityManager entityManager;

    @Value("${batch_size.manner_rank}")
    private int BATCH_SIZE;

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAll();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("매너 랭크 초기화")
    class ResetMannerRanksTest {

        @DisplayName("초기화 대상이 없는 경우")
        @Test
        void resetMannerRanksWhenNoTarget() {
            // given
            Member member1 = createMember("member1@gmail.com", "member1");
            Member member2 = createMember("member2@gmail.com", "member2");
            updateMannerScore(member2, 1);
            updateMannerRank(member2, 50.0);

            // when
            batchFacadeService.resetMannerRanks();

            // then
            assertThat(member1.getMannerRank()).isNull();
            assertThat(member2.getMannerRank()).isEqualTo(50.0);

            // 쿼리 실행 횟수 검증
            verify(entityManager, Mockito.times(0)).createNativeQuery(anyString());

        }

        @DisplayName("초기화 대상이 batch size 이하인 경우")
        @Test
        void resetMannerRanksWhenLessThanBatchSize() {
            // given
            Member member1 = createMember("member1@gmail.com", "member1");
            Member member2 = createMember("member2@gmail.com", "member2");
            updateMannerRank(member1, 25.0);
            updateMannerRank(member2, 50.0);

            // when
            batchFacadeService.resetMannerRanks();

            // then
            Member updatedMember1 = memberRepository.findById(member1.getId()).orElseThrow();
            Member updatedMember2 = memberRepository.findById(member2.getId()).orElseThrow();
            assertThat(updatedMember1.getMannerRank()).isNull();
            assertThat(updatedMember2.getMannerRank()).isNull();

            // 쿼리 실행 횟수 검증
            verify(entityManager, Mockito.times(1)).createNativeQuery(anyString());
        }

        @DisplayName("초기화 대상이 batch size 이상인 경우")
        @Test
        void resetMannerRanksWhenGreaterThanBatchSize() {
            // given
            List<Long> memberIds = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE + 10; i++) {
                Member member = createMember("member1@gmail.com", "member1");
                updateMannerRank(member, (double) i);
                memberIds.add(member.getId());
            }

            // when
            batchFacadeService.resetMannerRanks();

            // then
            memberIds.forEach(id -> {
                assertThat(memberRepository.findById(id).orElseThrow().getMannerRank()).isNull();
            });

            // 쿼리 실행 횟수 검증
            verify(entityManager, Mockito.times(2)).createNativeQuery(anyString());
        }

    }

    @Nested
    @DisplayName("매너 랭크 업데이트")
    class UpdateMannerRanksTest {

        @DisplayName("업데이트 대상이 없는 경우")
        @Test
        void updateMannerRanksWhenNoTarget() {
            // given
            Member member1 = createMember("member1@gmail.com", "member1");
            Member member2 = createMember("member2@gmail.com", "member2");

            // when
            batchFacadeService.updateMannerRanks();

            // then
            assertThat(member1.getMannerRank()).isNull();
            assertThat(member2.getMannerRank()).isNull();

            // 쿼리 실행 횟수 검증
            verify(entityManager, Mockito.times(0)).createNativeQuery(anyString());

        }

        @DisplayName("업데이트 대상이 batch size 이하인 경우")
        @Test
        void updateMannerRanksWhenLessThanBatchSize() {
            // given
            Member member1 = createMember("member1@gmail.com", "member1");
            Member member2 = createMember("member2@gmail.com", "member2");
            updateMannerScore(member1, 1);
            updateMannerScore(member2, 2);

            // when
            batchFacadeService.updateMannerRanks();

            // then
            Member updatedMember1 = memberRepository.findById(member1.getId()).orElseThrow();
            Member updatedMember2 = memberRepository.findById(member2.getId()).orElseThrow();
            assertThat(updatedMember1.getMannerRank()).isEqualTo(100.0);
            assertThat(updatedMember2.getMannerRank()).isEqualTo(50.0);

            // 쿼리 실행 횟수 검증
            verify(entityManager, Mockito.times(1)).createNativeQuery(anyString());
        }

        @DisplayName("업데이트 대상이 batch size 이상인 경우")
        @Test
        void updateMannerRanksWhenGreaterThanBatchSize() {
            // given
            List<Long> memberIds = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE + 20; i++) {
                Member member = createMember("member1@gmail.com", "member1");
                updateMannerScore(member, BATCH_SIZE + 20 - i);
                memberIds.add(member.getId());
            }

            // when
            batchFacadeService.updateMannerRanks();

            // then
            for (int i = 0; i < BATCH_SIZE + 20; i++) {
                Member member = memberRepository.findById(memberIds.get(i)).orElseThrow();
                assertThat(member.getMannerRank()).isEqualTo(2.0 * (i + 1));
            }

            // 쿼리 실행 횟수 검증
            verify(entityManager, Mockito.times(2)).createNativeQuery(anyString());
        }

    }

    private void updateMannerRank(Member member, Double rank) {
        member.updateMannerRank(rank);
        memberRepository.save(member);
    }

    private void updateMannerScore(Member member, Integer score) {
        member.updateMannerScore(score);
        memberRepository.save(member);
    }

    private Member createMember(String email, String gameName) {
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

}
