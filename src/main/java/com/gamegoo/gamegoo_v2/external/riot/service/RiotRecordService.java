package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.RiotMatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RiotRecordService {

    private final RestTemplate restTemplate;

    @Value("${spring.riot.api.key}")
    private String riotAPIKey;

    private static final String MATCH_IDS_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%s&api_key=%s";
    private static final String MATCH_INFO_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/lol/match/v5/matches/%s?api_key=%s";

    private static final int INITIAL_MATCH_COUNT = 20;
    private static final int MAX_MATCH_COUNT = 100;
    private static final int MATCH_INCREMENT = 10;
    private static final int MINIMUM_CHAMPIONS_REQUIRED = 3;

    /**
     * Riot API: 최근 선호 챔피언 3개 리스트 조회
     *
     * @param gameName 게임 이름
     * @param puuid    Riot PUUID
     * @return 선호 챔피언 ID 리스트
     */
    public List<ChampionStats> getPreferChampionfromMatch(String gameName, String puuid) {
        // 1. 최근 플레이한 챔피언 ID 리스트 가져오기
        Map<Long, ChampionStats> championStatsMap = fetchRecentChampionStats(gameName, puuid);

        // 2. 최소 요구 챔피언 수 확인
        if (championStatsMap.size() < MINIMUM_CHAMPIONS_REQUIRED) {
            throw new RiotException(ErrorCode.RIOT_INSUFFICIENT_MATCHES);
        }

        // 3. 많이 사용한 챔피언 상위 3개 계산
        return findTopChampions(championStatsMap, MINIMUM_CHAMPIONS_REQUIRED);
    }

    /**
     * 최근 플레이한 챔피언의 승/패 통계 정보를 Riot API에서 가져오는 메서드
     *
     * @param gameName 게임 이름
     * @param puuid    Riot PUUID
     * @return 챔피언별 통계 정보 맵
     */
    private Map<Long, ChampionStats> fetchRecentChampionStats(String gameName, String puuid) {
        Map<Long, ChampionStats> championStatsMap = new HashMap<>();
        int count = INITIAL_MATCH_COUNT;

        // 최소 요구 챔피언 수 이상일 때까지 반복
        while (championStatsMap.size() < MINIMUM_CHAMPIONS_REQUIRED && count <= MAX_MATCH_COUNT) {
            List<String> matchIds = fetchMatchIds(puuid, count);
            Map<Long, ChampionStats> matchStats = extractChampionStatsFromMatches(matchIds, gameName);

            // 각 매치에서 얻은 통계 정보를 병합
            for (Map.Entry<Long, ChampionStats> entry : matchStats.entrySet()) {
                championStatsMap.merge(entry.getKey(), entry.getValue(), (oldStats, newStats) -> {
                    oldStats.merge(newStats);
                    return oldStats;
                });
            }

            if (championStatsMap.size() < MINIMUM_CHAMPIONS_REQUIRED) {
                count += MATCH_INCREMENT;
            }
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
    private List<String> fetchMatchIds(String puuid, int count) {
        String url = String.format(MATCH_IDS_URL_TEMPLATE, puuid, count, riotAPIKey);
        try {
            // Riot API로부터 매칭 ID 리스트 가져오기
            String[] matchIds = restTemplate.getForObject(url, String[].class);
            return Arrays.asList(Objects.requireNonNull(matchIds));
        } catch (Exception e) {
            log.error("Failed to fetch match IDs for PUUID: {} with count: {}", puuid, count, e);
            throw new RiotException(ErrorCode.RIOT_MATCH_IDS_NOT_FOUND);
        }
    }

    /**
     * Riot API를 호출하여 매치 정보에서 사용자의 챔피언 ID와 승리 여부를 추출하는 메서드
     *
     * @param matchId  매칭 ID
     * @param gameName 사용자 게임 이름
     * @return 챔피언 통계 정보 (1경기 기준)
     */
    private ChampionStats fetchChampionStatsFromMatch(String matchId, String gameName) {
        String url = String.format(MATCH_INFO_URL_TEMPLATE, matchId, riotAPIKey);
        try {
            RiotMatchResponse response = restTemplate.getForObject(url, RiotMatchResponse.class);

            if (response == null || response.getInfo() == null || response.getInfo().getParticipants() == null) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }

            // 매치 정보에서 사용자의 챔피언과 승리 여부 추출
            RiotMatchResponse.ParticipantDTO participant = response.getInfo().getParticipants().stream()
                    .filter(p -> gameName.equals(p.getRiotIdGameName()))
                    .findFirst()
                    .orElseThrow(() -> new RiotException(ErrorCode.CHAMPION_NOT_FOUND));

            long championId = participant.getChampionId();
            boolean win = participant.isWin(); // 승리 여부 (RiotMatchResponse.ParticipantDTO에 isWin() 메서드가 있다고 가정)

            return new ChampionStats(championId, win);
        } catch (Exception e) {
            log.error("Failed to fetch champion stats for match ID: {}", matchId, e);
            throw new RiotException(ErrorCode.RIOT_MATCH_CHAMPION_NOT_FOUND);
        }
    }

    /**
     * 매칭 ID 리스트에서 각 매치별 챔피언 통계 정보를 추출하여 병합한 맵 반환
     *
     * @param matchIds 매칭 ID 리스트
     * @param gameName 게임 이름
     * @return 챔피언별 통계 정보 맵
     */
    private Map<Long, ChampionStats> extractChampionStatsFromMatches(List<String> matchIds, String gameName) {
        Map<Long, ChampionStats> statsMap = new HashMap<>();
        for (String matchId : matchIds) {
            try {
                ChampionStats stats = fetchChampionStatsFromMatch(matchId, gameName);
                if (stats != null && stats.getChampionId() < 1000) {
                    statsMap.merge(stats.getChampionId(), stats, (oldStats, newStats) -> {
                        oldStats.merge(newStats);
                        return oldStats;
                    });
                }
            } catch (Exception e) {
                log.error("Failed to fetch champion stats for match ID: {}", matchId, e);
            }
        }
        return statsMap;
    }


    /**
     * 챔피언별 통계 정보를 기반으로 승률(및 경기 수)을 고려하여 상위 N개 챔피언 ID를 계산
     *
     * @param championStatsMap 챔피언별 통계 정보 맵
     * @param topN             가져올 상위 챔피언 개수
     * @return 상위 챔피언 ID 리스트
     */
    private List<ChampionStats> findTopChampions(Map<Long, ChampionStats> championStatsMap, int topN) {
        return championStatsMap.values().stream()
                .filter(stats -> stats.getGames() > 0)
                .sorted(Comparator.comparingDouble(ChampionStats::getWinRate).reversed()
                        .thenComparing(ChampionStats::getGames).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }


}
