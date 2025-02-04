package com.gamegoo.gamegoo_v2.service.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.matching.service.MatchingScoreCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class MatchingScoreCalculatorTest {

    @Test
    @DisplayName("매너레벨 점수 계산: 동일한 매너 점수")
    void testGetMannerPriority_SameManner() {
        int result = MatchingScoreCalculator.getMannerPriority(5, 5, 16, 4);
        assertThat(result).isEqualTo(16);
    }

    @Test
    @DisplayName("매너레벨 점수 계산: 매너 차이가 있을 때")
    void testGetMannerPriority_DifferentManner() {
        int result = MatchingScoreCalculator.getMannerPriority(5, 1, 16, 4);
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("랭킹 우선순위 점수 계산: 동일한 티어 및 랭크")
    void testGetTierRankPriority_SameTierAndRank() {
        int result = MatchingScoreCalculator.getTierRankPriority(Tier.GOLD, 4, Tier.GOLD, 4, 40, 4);
        assertThat(result).isEqualTo(40);
    }

    @Test
    @DisplayName("랭킹 우선순위 점수 계산: 티어와 랭크 차이")
    void testGetTierRankPriority_DifferentTierAndRank() {
        int result = MatchingScoreCalculator.getTierRankPriority(Tier.GOLD, 1, Tier.SILVER, 2, 40, 4);
        assertThat(result).isEqualTo(35);
    }

    @Test
    @DisplayName("포지션 우선순위 점수 계산: 내가 원하는 포지션이 상대방 부포지션")
    void testGetPositionPriority_SameMainPosition() {
        int result = MatchingScoreCalculator.getPositionPriority(Position.TOP, Position.TOP, Position.MID, 3, 2, 1);
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("포지션 우선순위 점수 계산: 내가 원하는 포지션이 상대방 부 포지션")
    void testGetPositionPriority_SubPositionMatch() {
        int result = MatchingScoreCalculator.getPositionPriority(Position.MID, Position.TOP, Position.MID, 3, 2, 1);
        assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("포지션 우선순위 점수 계산: ANY 포지션 포함")
    void testGetPositionPriority_AnyPosition() {
        int result = MatchingScoreCalculator.getPositionPriority(Position.ANY, Position.JUNGLE, Position.MID, 50, 30,
                10);
        assertThat(result).isEqualTo(50);
    }

    @Test
    @DisplayName("마이크 우선순위 점수 계산: 동일한 마이크 설정")
    void testGetMikePriority_SameMike() {
        int result = MatchingScoreCalculator.getMikePriority(Mike.AVAILABLE, Mike.AVAILABLE, 2);
        assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("마이크 우선순위 점수 계산: 다른 마이크 설정")
    void testGetMikePriority_DifferentMike() {
        int result = MatchingScoreCalculator.getMikePriority(Mike.AVAILABLE, Mike.UNAVAILABLE, 2);
        assertThat(result).isEqualTo(0);
    }

}
