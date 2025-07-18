package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotMatchResponse;
import com.gamegoo.gamegoo_v2.utils.ChampionIdStore;
import com.gamegoo.gamegoo_v2.utils.RiotApiHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.Builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RiotRecordService {

    private final RestTemplate restTemplate;
    private final RiotApiHelper riotApiHelper;

    @Value("${spring.riot.api.key}")
    private String riotAPIKey;

    private static final String MATCH_IDS_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/lol/match/v5/matches/by-puuid/%s/ids?start=%s&count=%s&api_key=%s";
    private static final String MATCH_INFO_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/lol/match/v5/matches/%s?api_key=%s";

    private static final int INITIAL_MATCH_COUNT = 30;
    private static final int MAX_CHAMPIONS_REQUIRED = 4;

    @Getter
    @Builder
    public static class Recent30GameStatsResponse {
        private int recTotalWins;
        private int recTotalLosses;
        private double recWinRate;
        private double recAvgKDA;
        private double recAvgKills;
        private double recAvgDeaths;
        private double recAvgAssists;
        private double recAvgCsPerMinute;
        private int recTotalCs;
    }

    /**
     * Riot API: 최근 선호 챔피언 4개 리스트 조회
     *
     * @param gameName 게임 이름
     * @param puuid    Riot PUUID
     * @return 선호 챔피언 ID 리스트
     */
    public List<ChampionStats> getPreferChampionfromMatch(String gameName, String puuid) {
        // 1. 최근 플레이한 챔피언 ID 리스트 가져오기
        Map<Long, ChampionStats> championStatsMap = fetchRecentChampionStats(gameName, puuid);

        // 2. 많이 사용한 챔피언 상위 최대 4개 계산
        return championStatsMap.values().stream()
                .filter(stats -> stats.getGames() > 0)
                .sorted(Comparator.comparingInt(ChampionStats::getGames).reversed())
                .limit(MAX_CHAMPIONS_REQUIRED)
                .collect(Collectors.toList());
    }

    /**
     * 최근 플레이한 챔피언 ID 리스트를 Riot API에서 가져오는 메서드
     *
     * @param gameName 게임 이름
     * @param puuid    Riot PUUID
     * @return 챔피언 ID 리스트
     */
    private Map<Long, ChampionStats> fetchRecentChampionStats(String gameName, String puuid) {
        Map<Long, ChampionStats> championStatsMap = new HashMap<>();

        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        for (String matchId : matchIds) {
            Optional<ChampionStats> championStatsOpt = fetchChampionStatsFromMatch(matchId, gameName);
            championStatsOpt.ifPresent(stats -> {
                if (ChampionIdStore.contains(stats.getChampionId())) {
                    championStatsMap.merge(stats.getChampionId(), stats, (oldStats, newStats) -> {
                        oldStats.merge(newStats);
                        return oldStats;
                    });
                }
            });
        }

        return championStatsMap;
    }

    /**
     * Riot API를 호출하여 puuid에 해당하는 최근 매칭 ID를 가져오는 메서드
     *
     * @param puuid Riot PUUID
     * @param count 가져올 매칭 개수
     * @return 매칭 ID 리스트
     */
    private List<String> fetchMatchIds(String puuid, int start, int count) {
        String url = String.format(MATCH_IDS_URL_TEMPLATE, puuid, start, count, riotAPIKey);
        try {
            // Riot API로부터 매칭 ID 리스트 가져오기
            String[] matchIds = restTemplate.getForObject(url, String[].class);
            return Arrays.asList(Objects.requireNonNull(matchIds));
        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return Collections.emptyList();
        }
    }

    /**
     * Riot API를 호출하여 매칭 ID로부터 특정 사용자의 챔피언 ID를 가져오는 메서드
     *
     * @param matchId  매칭 ID
     * @param gameName 소환사명
     * @return 챔피언 ID
     */
    private Optional<ChampionStats> fetchChampionStatsFromMatch(String matchId, String gameName) {
        String url = String.format(MATCH_INFO_URL_TEMPLATE, matchId, riotAPIKey);

        try {
            // Riot API로부터 매칭 정보를 가져오기
            RiotMatchResponse response = restTemplate.getForObject(url, RiotMatchResponse.class);
            if (response == null || response.getInfo() == null || response.getInfo().getParticipants() == null) {
                return Optional.empty();
            }

            // 솔로 랭크(420)와 자유 랭크(440)가 아니면 통계에 포함하지 않음
            int queueId = response.getInfo().getQueueId();
            if (queueId != 420 && queueId != 440) {
                return Optional.empty();
            }

            int gameDuration = response.getInfo().getGameDuration();
            // 게임 시간이 0이면 기본값 30분(1800초)으로 설정
            if (gameDuration <= 0) {
                gameDuration = 1800;
            }

            final int finalGameDuration = gameDuration;

            return response.getInfo().getParticipants().stream()
                    .filter(participant -> gameName.equals(participant.getRiotIdGameName()))
                    .findFirst()
                    .map(participant -> {
                        ChampionStats stats = new ChampionStats(participant.getChampionId(), participant.isWin());
                        stats.setGameTime(finalGameDuration);
                        // CS가 음수인 경우 0으로 설정 (미니언 + 정글몹)
                        int totalCs = Math.max(0, participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled());
                        stats.setTotalMinionsKilled(totalCs);
                        // KDA 정보 설정
                        stats.setKills(participant.getKills());
                        stats.setDeaths(participant.getDeaths());
                        stats.setAssists(participant.getAssists());
                        return stats;
                    });

        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return Optional.empty();
        }
    }

    public Recent30GameStatsResponse getRecent30GameStats(String gameName, String puuid) {
        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        int totalWins = 0;
        int totalLosses = 0;
        int totalKills = 0;
        int totalDeaths = 0;
        int totalAssists = 0;
        int totalCs = 0;
        double totalCsPerMinute = 0.0;
        int totalGames = 0;

        for (String matchId : matchIds) {
            Optional<ChampionStats> championStatsOpt = fetchChampionStatsFromMatch(matchId, gameName);
            if (championStatsOpt.isPresent()) {
                ChampionStats stats = championStatsOpt.get();
                totalGames++;
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
        }
        double recWinRate = totalGames > 0 ? (double) totalWins / totalGames * 100 : 0.0;
        double recAvgKDA = totalGames > 0 ? (double) (totalKills + totalAssists) / Math.max(1, totalDeaths) : 0.0;
        double recAvgCsPerMinute = totalGames > 0 ? totalCsPerMinute / totalGames : 0.0;
        return Recent30GameStatsResponse.builder()
                .recTotalWins(totalWins)
                .recTotalLosses(totalLosses)
                .recWinRate(recWinRate)
                .recAvgKDA(recAvgKDA)
                .recAvgKills(totalGames > 0 ? (double) totalKills / totalGames : 0.0)
                .recAvgDeaths(totalGames > 0 ? (double) totalDeaths / totalGames : 0.0)
                .recAvgAssists(totalGames > 0 ? (double) totalAssists / totalGames : 0.0)
                .recAvgCsPerMinute(recAvgCsPerMinute)
                .recTotalCs((totalWins + totalLosses) > 0 ? totalCs / (totalWins + totalLosses) : 0)
                .build();
    }

}
