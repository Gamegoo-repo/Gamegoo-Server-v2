package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Recent30GameStatsResponse {
    private int recTotalWins;
    private int recTotalLosses;
    private double recWinRate;
    private double recAvgKDA;
    private double recAvgKills;
    private double recAvgDeaths;
    private double recAvgAssists;
    private double recAvgCsPerMinute;
    private int recTotalCs;

    public static Recent30GameStatsResponse fromChampionStats(List<ChampionStats> statsList) {
        int totalWins = 0;
        int totalLosses = 0;
        int totalKills = 0;
        int totalDeaths = 0;
        int totalAssists = 0;
        int totalCs = 0;
        double totalCsPerMinute = 0.0;

        for (ChampionStats stats : statsList) {
            if (stats.getWins() > 0) totalWins++;
            else totalLosses++;
            totalKills += stats.getKills();
            totalDeaths += stats.getDeaths();
            totalAssists += stats.getAssists();
            totalCs += stats.getTotalMinionsKilled();
            double gameMinutes = stats.getGameTime() / 60.0;
            if (gameMinutes > 0) {
                totalCsPerMinute += stats.getTotalMinionsKilled() / gameMinutes;
            }
        }

        return from(totalWins, totalLosses, totalKills, totalDeaths,
                    totalAssists, totalCs, totalCsPerMinute, statsList.size());
    }

    public static Recent30GameStatsResponse from(int totalWins, int totalLosses, int totalKills,
                                                  int totalDeaths, int totalAssists, int totalCs,
                                                  double totalCsPerMinute, int totalGames) {
        if (totalGames == 0) {
            return Recent30GameStatsResponse.builder()
                    .recTotalWins(0)
                    .recTotalLosses(0)
                    .recWinRate(0.0)
                    .recAvgKDA(0.0)
                    .recAvgKills(0.0)
                    .recAvgDeaths(0.0)
                    .recAvgAssists(0.0)
                    .recAvgCsPerMinute(0.0)
                    .recTotalCs(0)
                    .build();
        }

        double winRate = (double) totalWins / totalGames * 100;
        double avgKDA = totalDeaths == 0 ? (totalKills + totalAssists) : (totalKills + totalAssists) / (double) totalDeaths;
        double avgKills = (double) totalKills / totalGames;
        double avgDeaths = (double) totalDeaths / totalGames;
        double avgAssists = (double) totalAssists / totalGames;
        double avgCsPerMinute = totalCsPerMinute / totalGames;

        return Recent30GameStatsResponse.builder()
                .recTotalWins(totalWins)
                .recTotalLosses(totalLosses)
                .recWinRate(Math.round(winRate * 100) / 100.0)
                .recAvgKDA(Math.round(avgKDA * 100) / 100.0)
                .recAvgKills(Math.round(avgKills * 100) / 100.0)
                .recAvgDeaths(Math.round(avgDeaths * 100) / 100.0)
                .recAvgAssists(Math.round(avgAssists * 100) / 100.0)
                .recAvgCsPerMinute(Math.round(avgCsPerMinute * 10) / 10.0)
                .recTotalCs(totalCs)
                .build();
    }
}
