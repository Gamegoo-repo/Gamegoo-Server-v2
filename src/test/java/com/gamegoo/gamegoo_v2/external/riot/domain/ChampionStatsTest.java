package com.gamegoo.gamegoo_v2.external.riot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChampionStatsTest {

    @Test
    @DisplayName("KDA 계산 테스트")
    void testKDA() {
        ChampionStats stats = new ChampionStats(1L, true);
        
        // 1. 기본 KDA 계산 (킬: 5, 데스: 2, 어시스트: 3)
        stats.setKills(5);
        stats.setDeaths(2);
        stats.setAssists(3);
        assertEquals(4.0, stats.getKDA()); // (5 + 3) / 2 = 4.0

        // 2. 데스가 0인 경우 (킬: 3, 데스: 0, 어시스트: 2)
        stats.setKills(3);
        stats.setDeaths(0);
        stats.setAssists(2);
        assertEquals(5.0, stats.getKDA()); // 3 + 2 = 5.0

        // 3. 모든 값이 0인 경우
        stats.setKills(0);
        stats.setDeaths(0);
        stats.setAssists(0);
        assertEquals(0.0, stats.getKDA()); // 0 반환

        // 4. 킬과 어시스트만 있는 경우 (킬: 2, 데스: 0, 어시스트: 1)
        stats.setKills(2);
        stats.setDeaths(0);
        stats.setAssists(1);
        assertEquals(3.0, stats.getKDA()); // 2 + 1 = 3.0

        // 5. 데스만 있는 경우 (킬: 0, 데스: 1, 어시스트: 0)
        stats.setKills(0);
        stats.setDeaths(1);
        stats.setAssists(0);
        assertEquals(0.0, stats.getKDA()); // 0 / 1 = 0.0
    }
} 