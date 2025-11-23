package com.gamegoo.gamegoo_v2.external.riot.util;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.AllModeStatsResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.Recent30GameStatsResponse;
import com.gamegoo.gamegoo_v2.utils.ChampionIdStore;

import java.util.HashMap;
import java.util.Map;

/**
 * 모드별(솔로랭크/자유랭크/칼바람) 게임 통계 수집 유틸리티
 */
public class AllModeStatsCollector {

    private static final int QUEUE_ID_SOLO_RANK = 420;
    private static final int QUEUE_ID_FREE_RANK = 440;
    private static final int QUEUE_ID_ARAM = 450;

    private final int[] totalWins = new int[4];      // [combined, solo, free, aram]
    private final int[] totalLosses = new int[4];
    private final int[] totalKills = new int[4];
    private final int[] totalDeaths = new int[4];
    private final int[] totalAssists = new int[4];
    private final int[] totalCs = new int[4];
    private final double[] totalCsPerMinute = new double[4];
    private final int[] totalGames = new int[4];

    private final Map<Long, ChampionStats> combinedChampionStats = new HashMap<>();
    private final Map<Long, ChampionStats> soloChampionStats = new HashMap<>();
    private final Map<Long, ChampionStats> freeChampionStats = new HashMap<>();
    private final Map<Long, ChampionStats> aramChampionStats = new HashMap<>();

    public void processChampionStats(ChampionStats stats) {
        int queueId = stats.getQueueId();

        if (ChampionIdStore.contains(stats.getChampionId())) {
            updateChampionStatsByMode(stats, queueId);
        }

        updateGameStatsByMode(stats, queueId);
    }

    private void updateChampionStatsByMode(ChampionStats stats, int queueId) {
        switch (queueId) {
            case QUEUE_ID_SOLO_RANK -> soloChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
            case QUEUE_ID_FREE_RANK -> freeChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
            case QUEUE_ID_ARAM -> aramChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
        }

        if (queueId == QUEUE_ID_SOLO_RANK || queueId == QUEUE_ID_FREE_RANK) {
            combinedChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
        }
    }

    private void updateGameStatsByMode(ChampionStats stats, int queueId) {
        double gameMinutes = stats.getGameTime() / 60.0;
        double csPerMinute = gameMinutes > 0 ? stats.getTotalMinionsKilled() / gameMinutes : 0.0;

        if (queueId == QUEUE_ID_SOLO_RANK || queueId == QUEUE_ID_FREE_RANK) {
            updateStatsArray(0, stats, csPerMinute);
        }

        switch (queueId) {
            case QUEUE_ID_SOLO_RANK -> updateStatsArray(1, stats, csPerMinute);
            case QUEUE_ID_FREE_RANK -> updateStatsArray(2, stats, csPerMinute);
            case QUEUE_ID_ARAM -> updateStatsArray(3, stats, csPerMinute);
        }
    }

    private void updateStatsArray(int index, ChampionStats stats, double csPerMinute) {
        totalGames[index]++;
        if (stats.getWins() > 0) totalWins[index]++;
        else totalLosses[index]++;
        totalKills[index] += stats.getKills();
        totalDeaths[index] += stats.getDeaths();
        totalAssists[index] += stats.getAssists();
        totalCs[index] += stats.getTotalMinionsKilled();
        totalCsPerMinute[index] += csPerMinute;
    }

    private ChampionStats mergeChampionStats(ChampionStats oldStats, ChampionStats newStats) {
        oldStats.merge(newStats);
        return oldStats;
    }

    public AllModeStatsResponse buildResponse() {
        Recent30GameStatsResponse combinedStats = buildStatsResponse(
                totalWins[0], totalLosses[0], totalKills[0], totalDeaths[0],
                totalAssists[0], totalCs[0], totalCsPerMinute[0], totalGames[0]);
        Recent30GameStatsResponse soloStats = buildStatsResponse(
                totalWins[1], totalLosses[1], totalKills[1], totalDeaths[1],
                totalAssists[1], totalCs[1], totalCsPerMinute[1], totalGames[1]);
        Recent30GameStatsResponse freeStats = buildStatsResponse(
                totalWins[2], totalLosses[2], totalKills[2], totalDeaths[2],
                totalAssists[2], totalCs[2], totalCsPerMinute[2], totalGames[2]);
        Recent30GameStatsResponse aramStats = buildStatsResponse(
                totalWins[3], totalLosses[3], totalKills[3], totalDeaths[3],
                totalAssists[3], totalCs[3], totalCsPerMinute[3], totalGames[3]);

        return AllModeStatsResponse.builder()
                .combinedStats(combinedStats)
                .soloStats(soloStats)
                .freeStats(freeStats)
                .aramStats(aramStats)
                .combinedChampionStats(combinedChampionStats)
                .soloChampionStats(soloChampionStats)
                .freeChampionStats(freeChampionStats)
                .aramChampionStats(aramChampionStats)
                .build();
    }

    private Recent30GameStatsResponse buildStatsResponse(
            int wins, int losses, int kills, int deaths, int assists,
            int cs, double csPerMinute, int games) {
        return Recent30GameStatsResponse.from(wins, losses, kills, deaths, assists, cs, csPerMinute, games);
    }
}
