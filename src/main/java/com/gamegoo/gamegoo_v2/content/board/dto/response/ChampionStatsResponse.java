package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.MemberChampion;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChampionStatsResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private long championId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String championName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double winRate;      // 승률
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int wins;            // 승 수
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int games;           // 판 수
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double csPerMinute;  // 분당 CS
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double averageCs;    // 평균 CS
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double kda;          // KDA
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double kills;        // 평균 킬
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double deaths;       // 평균 데스
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private double assists;      // 평균 어시스트

    public static ChampionStatsResponse from(ChampionStats stats, Champion champion) {
        return ChampionStatsResponse.builder()
                .championId(stats.getChampionId())
                .championName(champion.getName())
                .winRate(stats.getWinRate())
                .wins(stats.getWins())
                .games(stats.getGames())
                .csPerMinute(stats.getCsPerMinute())
                .averageCs(stats.getGames() > 0 ? (double) stats.getTotalCs() / stats.getGames() : 0)
                .kda(stats.getKDA())
                .kills(stats.getGames() > 0 ? (double) stats.getKills() / stats.getGames() : 0)
                .deaths(stats.getGames() > 0 ? (double) stats.getDeaths() / stats.getGames() : 0)
                .assists(stats.getGames() > 0 ? (double) stats.getAssists() / stats.getGames() : 0)
                .build();
    }

    public static ChampionStatsResponse from(MemberChampion memberChampion) {
        Champion champion = memberChampion.getChampion();
        int wins = memberChampion.getWins();
        int games = memberChampion.getGames();
        double winRate = games > 0 ? ((double) wins / games) * 100 : 0.0;
        return ChampionStatsResponse.builder()
                .championId(champion.getId())
                .championName(champion.getName())
                .winRate(winRate)
                .wins(wins)
                .games(games)
                .csPerMinute(memberChampion.getCsPerMinute())
                .averageCs(games > 0 ? (double) memberChampion.getTotalCs() / games : 0)
                .kda(memberChampion.getKDA())
                .kills(games > 0 ? (double) memberChampion.getKills() / games : 0)
                .deaths(games > 0 ? (double) memberChampion.getDeaths() / games : 0)
                .assists(games > 0 ? (double) memberChampion.getAssists() / games : 0)
                .build();
    }

}
