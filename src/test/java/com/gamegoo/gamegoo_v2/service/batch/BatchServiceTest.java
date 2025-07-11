package com.gamegoo.gamegoo_v2.service.batch;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.core.batch.BatchService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
public class BatchServiceTest {

    @Autowired
    BatchService batchService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberRecentStatsRepository memberRecentStatsRepository;

    @MockitoSpyBean
    EntityManager entityManager;

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAll();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("매너 랭크 배치 업데이트")
    class BatchUpdateMannerRanksTest {

        @DisplayName("map이 비어있는 경우")
        @Test
        void batchUpdateMannerRanksWhenMapIsEmpty() {
            // given
            Member member = createMember("member1@gmail.com", "member1");

            Map<Long, Double> emptyMap = Collections.emptyMap();
            List<Map.Entry<Long, Double>> entries = new ArrayList<>(emptyMap.entrySet());

            // when
            batchService.batchUpdateMannerRanks(entries);

            // then
            Member resultMember = memberRepository.findById(member.getId()).orElseThrow();
            assertThat(resultMember.getMannerRank()).isNull();
        }

        @DisplayName("mannerRank 값이 null인 경우")
        @Test
        void batchUpdateMannerRanksWhenValueIsNull() {
            // given
            Member member = createMember("test@email.com", "testMember");
            updateMannerRank(member, 1.0);

            Map<Long, Double> mannerRankMap = new HashMap<>();
            mannerRankMap.put(member.getId(), null);

            List<Map.Entry<Long, Double>> entries = new ArrayList<>(mannerRankMap.entrySet());

            // when
            batchService.batchUpdateMannerRanks(entries);

            // then
            Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
            assertThat(updatedMember.getMannerRank()).isNull();
        }

        @DisplayName("mannerRank 값이 null이 아닌 경우")
        @Test
        void batchUpdateMannerRanksWhenValueIsNotNull() {
            // given
            Member member = createMember("test@email.com", "testMember");
            updateMannerRank(member, 1.0);

            Map<Long, Double> mannerRankMap = new HashMap<>();
            mannerRankMap.put(member.getId(), 50.0);

            List<Map.Entry<Long, Double>> entries = new ArrayList<>(mannerRankMap.entrySet());

            // when
            batchService.batchUpdateMannerRanks(entries);

            // then
            Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
            assertThat(updatedMember.getMannerRank()).isEqualTo(50.0);
        }

        @DisplayName("batch size만큼의 업데이트 요청인 경우")
        @Test
        void batchUpdateMannerRanksWithMaxSize() {
            // given
            int batchSize = 30;
            List<Member> members = new ArrayList<>();
            Map<Long, Double> mannerRankMap = new HashMap<>();

            for (int i = 0; i < batchSize; i++) {
                Member member = createMember("member" + i + "@gmail.com", "member" + i);
                members.add(member);
                mannerRankMap.put(member.getId(), (double) (i + 1));
            }

            List<Map.Entry<Long, Double>> entries = new ArrayList<>(mannerRankMap.entrySet());

            // when
            batchService.batchUpdateMannerRanks(entries);

            // then
            // mannerRank 업데이트 검증
            for (int i = 0; i < batchSize; i++) {
                Member updatedMember = memberRepository.findById(members.get(i).getId()).orElseThrow();
                assertThat(updatedMember.getMannerRank()).isEqualTo((double) (i + 1));
            }

            // 쿼리 실행 횟수 검증
            verify(entityManager, Mockito.times(1)).createNativeQuery(anyString());
        }

    }

    private void updateMannerRank(Member member, Double rank) {
        member.updateMannerRank(rank);
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
