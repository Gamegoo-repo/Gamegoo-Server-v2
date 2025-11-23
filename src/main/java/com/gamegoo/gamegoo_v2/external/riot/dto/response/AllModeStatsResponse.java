package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

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
}
