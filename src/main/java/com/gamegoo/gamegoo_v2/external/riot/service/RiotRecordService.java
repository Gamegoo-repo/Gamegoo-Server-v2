package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.RiotMatchResponse;
import com.gamegoo.gamegoo_v2.utils.ChampionIdStore;
import com.gamegoo.gamegoo_v2.utils.RiotApiHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
            ".com/lol/match/v5/matches/by-puuid/%s/ids?start%s0&count=%s&api_key=%s";
    private static final String MATCH_INFO_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/lol/match/v5/matches/%s?api_key=%s";

    private static final int INITIAL_MATCH_COUNT = 20;
    private static final int MAX_MATCH_COUNT = 100;
    private static final int MAX_CHAMPIONS_REQUIRED = 3;

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

        // 2. 많이 사용한 챔피언 상위 최대 3개 계산
        return championStatsMap.values().stream()
                .filter(stats -> stats.getGames() > 0)
                .sorted(Comparator.comparingDouble(ChampionStats::getWinRate).reversed()
                        .thenComparing(ChampionStats::getGames).reversed())
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
        int count = INITIAL_MATCH_COUNT;
        int start = 0;

        while (championStatsMap.size() < MAX_CHAMPIONS_REQUIRED && start + count <= MAX_MATCH_COUNT) {
            List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, start, count))
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

            // 챔피언 수가 부족하면 더 많은 데이터를 요청하기 위해 start 증가
            if (championStatsMap.size() < MAX_CHAMPIONS_REQUIRED) {
                start += 20; // 추가 데이터 요청을 위해 start 값을 증가
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

            return response.getInfo().getParticipants().stream()
                    .filter(participant -> gameName.equals(participant.getRiotIdGameName()))
                    .findFirst()
                    .map(participant -> new ChampionStats(participant.getChampionId(), participant.isWin()));

        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return Optional.empty();
        }
    }

}
