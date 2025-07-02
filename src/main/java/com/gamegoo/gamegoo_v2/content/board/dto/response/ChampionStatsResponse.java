package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberChampion;
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
    private double averageCs;    // 평균 CS
    private double kda;          // KDA
    private double kills;        // 평균 킬
    private double deaths;       // 평균 데스
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
        double winRate = games > 0 ? (double) wins / games : 0.0;
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

