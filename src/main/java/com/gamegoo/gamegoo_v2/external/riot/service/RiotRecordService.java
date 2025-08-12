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
    
    // 큐 ID 상수
    private static final int QUEUE_ID_SOLO_RANK = 420;
    private static final int QUEUE_ID_FREE_RANK = 440;
    private static final int QUEUE_ID_ARAM = 450;

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
     * Riot API: 특정 큐 ID의 최근 선호 챔피언 4개 리스트 조회
     *
     * @param gameName 게임 이름
     * @param puuid    Riot PUUID
     * @param targetQueueId 대상 큐 ID (420: 솔로, 440: 자유, 450: 칼바람)
     * @return 선호 챔피언 ID 리스트
     */
    public List<ChampionStats> getPreferChampionFromMatchByQueueId(String gameName, String puuid, int targetQueueId) {
        // 1. 특정 큐 ID의 최근 플레이한 챔피언 통계 가져오기
        Map<Long, ChampionStats> championStatsMap = fetchRecentChampionStatsByQueueId(gameName, puuid, targetQueueId);

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
     * 특정 큐 ID의 최근 플레이한 챔피언 통계를 Riot API에서 가져오는 메서드
     *
     * @param gameName 게임 이름
     * @param puuid    Riot PUUID
     * @param targetQueueId 대상 큐 ID
     * @return 큐 ID별 챔피언 통계 맵
     */
    private Map<Long, ChampionStats> fetchRecentChampionStatsByQueueId(String gameName, String puuid, int targetQueueId) {
        Map<Long, ChampionStats> championStatsMap = new HashMap<>();

        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        for (String matchId : matchIds) {
            Optional<ChampionStats> championStatsOpt = fetchChampionStatsFromMatch(matchId, gameName);
            championStatsOpt.ifPresent(stats -> {
                // 특정 큐 ID의 게임만 포함
                if (stats.getQueueId() == targetQueueId && ChampionIdStore.contains(stats.getChampionId())) {
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

            // 솔로 랭크, 자유 랭크, 칼바람이 아니면 통계에 포함하지 않음
            int queueId = response.getInfo().getQueueId();
            if (queueId != QUEUE_ID_SOLO_RANK && queueId != QUEUE_ID_FREE_RANK && queueId != QUEUE_ID_ARAM) {
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
                        stats.setQueueId(queueId); // Queue ID 설정 추가
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

    public Recent30GameStatsResponse getRecent30GameStatsByQueueId(String gameName, String puuid, int targetQueueId) {
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
                // 특정 큐 ID의 게임만 포함
                if (stats.getQueueId() == targetQueueId) {
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
                // 프로필용: 솔로와 자유만 포함, 칼바람 제외
                if (stats.getQueueId() == QUEUE_ID_SOLO_RANK || stats.getQueueId() == QUEUE_ID_FREE_RANK) {
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

    /**
     * 한 번의 API 호출로 모든 모드의 통계를 계산하는 최적화된 메서드
     */
    @Getter
    @Builder
    public static class AllModeStatsResponse {
        private Recent30GameStatsResponse combinedStats;  // 프로필용 (솔로+자유)
        private Recent30GameStatsResponse soloStats;      // 솔로 전용
        private Recent30GameStatsResponse freeStats;      // 자유 전용  
        private Recent30GameStatsResponse aramStats;      // 칼바람 전용
        private Map<Long, ChampionStats> combinedChampionStats;  // 프로필용 챔피언 통계
        private Map<Long, ChampionStats> soloChampionStats;      // 솔로 챔피언 통계
        private Map<Long, ChampionStats> freeChampionStats;      // 자유 챔피언 통계
        private Map<Long, ChampionStats> aramChampionStats;      // 칼바람 챔피언 통계
    }

    /**
     * 최적화된 메서드: 한 번의 API 호출로 모든 모드별 통계를 계산
     */
    public AllModeStatsResponse getAllModeStatsOptimized(String gameName, String puuid) {
        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        AllModeStatsCollector collector = new AllModeStatsCollector();
        
        // 한 번의 루프로 모든 매치 데이터 처리
        for (String matchId : matchIds) {
            Optional<ChampionStats> championStatsOpt = fetchChampionStatsFromMatch(matchId, gameName);
            championStatsOpt.ifPresent(collector::processChampionStats);
        }

        return collector.buildResponse();
    }

    /**
     * 모든 모드의 통계를 수집하는 헬퍼 클래스
     */
    private class AllModeStatsCollector {
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
            
            // 챔피언 통계 업데이트
            if (ChampionIdStore.contains(stats.getChampionId())) {
                updateChampionStatsByMode(stats, queueId);
            }

            // 게임 통계 업데이트
            updateGameStatsByMode(stats, queueId);
        }

        private void updateChampionStatsByMode(ChampionStats stats, int queueId) {
            // 모드별 챔피언 통계 저장
            switch (queueId) {
                case QUEUE_ID_SOLO_RANK -> soloChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
                case QUEUE_ID_FREE_RANK -> freeChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
                case QUEUE_ID_ARAM -> aramChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
            }
            
            // 프로필용 통합 챔피언 통계 (솔로+자유만)
            if (queueId == QUEUE_ID_SOLO_RANK || queueId == QUEUE_ID_FREE_RANK) {
                combinedChampionStats.merge(stats.getChampionId(), stats, this::mergeChampionStats);
            }
        }

        private void updateGameStatsByMode(ChampionStats stats, int queueId) {
            double gameMinutes = stats.getGameTime() / 60.0;
            double csPerMinute = gameMinutes > 0 ? stats.getTotalMinionsKilled() / gameMinutes : 0.0;

            // 프로필용 통합 통계 (솔로+자유)
            if (queueId == QUEUE_ID_SOLO_RANK || queueId == QUEUE_ID_FREE_RANK) {
                updateStatsArray(0, stats, csPerMinute);
            }

            // 각 모드별 통계
            switch (queueId) {
                case QUEUE_ID_SOLO_RANK -> updateStatsArray(1, stats, csPerMinute); // 솔로
                case QUEUE_ID_FREE_RANK -> updateStatsArray(2, stats, csPerMinute); // 자유
                case QUEUE_ID_ARAM -> updateStatsArray(3, stats, csPerMinute); // 칼바람
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
            Recent30GameStatsResponse combinedStats = buildStatsResponse(totalWins[0], totalLosses[0], 
                    totalKills[0], totalDeaths[0], totalAssists[0], totalCs[0], totalCsPerMinute[0], totalGames[0]);
            Recent30GameStatsResponse soloStats = buildStatsResponse(totalWins[1], totalLosses[1], 
                    totalKills[1], totalDeaths[1], totalAssists[1], totalCs[1], totalCsPerMinute[1], totalGames[1]);
            Recent30GameStatsResponse freeStats = buildStatsResponse(totalWins[2], totalLosses[2], 
                    totalKills[2], totalDeaths[2], totalAssists[2], totalCs[2], totalCsPerMinute[2], totalGames[2]);
            Recent30GameStatsResponse aramStats = buildStatsResponse(totalWins[3], totalLosses[3], 
                    totalKills[3], totalDeaths[3], totalAssists[3], totalCs[3], totalCsPerMinute[3], totalGames[3]);

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
    }

    private Recent30GameStatsResponse buildStatsResponse(int totalWins, int totalLosses, int totalKills, 
                                                        int totalDeaths, int totalAssists, int totalCs, 
                                                        double totalCsPerMinute, int totalGames) {
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
