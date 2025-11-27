package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class AllModeStatsResponse {
    private Recent30GameStatsResponse combinedStats;  // 프로필용 (솔로+자유)
    private Recent30GameStatsResponse soloStats;      // 솔로 전용
    private Recent30GameStatsResponse freeStats;      // 자유 전용
    private Recent30GameStatsResponse aramStats;      // 칼바람 전용
    private Map<Long, ChampionStats> combinedChampionStats;  // 프로필용 챔피언 통계
    private Map<Long, ChampionStats> soloChampionStats;      // 솔로 챔피언 통계
    private Map<Long, ChampionStats> freeChampionStats;      // 자유 챔피언 통계
    private Map<Long, ChampionStats> aramChampionStats;      // 칼바람 챔피언 통계

    /**
     * 챔피언 통계 맵에서 상위 4개 챔피언 추출
     */
    private static List<ChampionStats> getTopChampions(Map<Long, ChampionStats> championStats) {
        return championStats.values().stream()
                .filter(stats -> stats.getGames() > 0)
                .sorted(Comparator.comparingInt(ChampionStats::getGames).reversed())
                .limit(4)
                .collect(Collectors.toList());
    }

    public List<ChampionStats> getTopCombinedChampions() {
        return getTopChampions(combinedChampionStats);
    }

    public List<ChampionStats> getTopSoloChampions() {
        return getTopChampions(soloChampionStats);
    }

    public List<ChampionStats> getTopFreeChampions() {
        return getTopChampions(freeChampionStats);
    }

    public List<ChampionStats> getTopAramChampions() {
        return getTopChampions(aramChampionStats);
    }
}
