package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChampionStatsResponse {

    private long championId;
    private String championName;
    private double winRate;      // 승률
    private int wins;            // 승 수
    private int games;           // 판 수
    private double csPerMinute;  // 분당 CS
    private double kda;          // KDA
    private int kills;           // 킬
    private int deaths;          // 데스
    private int assists;         // 어시스트

    public static ChampionStatsResponse from(ChampionStats stats, Champion champion) {
        return ChampionStatsResponse.builder()
                .championId(stats.getChampionId())
                .championName(champion.getName())
                .winRate(stats.getWinRate())
                .wins(stats.getWins())
                .games(stats.getGames())
                .csPerMinute(stats.getCsPerMinute())
                .kda(stats.getKDA())
                .kills(stats.getKills())
                .deaths(stats.getDeaths())
                .assists(stats.getAssists())
                .build();
    }

}

