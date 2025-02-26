package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.dto.RiotMatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
     * @param gameName  게임 이름
     * @param puuid     Riot PUUID
     * @return          선호 챔피언 ID 리스트
     */
    public List<Long> getPreferChampionfromMatch(String gameName, String puuid) {
        // 1. 최근 플레이한 챔피언 ID 리스트 가져오기
        List<Long> recentChampionIds = fetchRecentChampionIds(gameName, puuid);

        // 2. 최소 요구 챔피언 수 확인
        if (recentChampionIds.size() < MINIMUM_CHAMPIONS_REQUIRED) {
            throw new RiotException(ErrorCode.RIOT_INSUFFICIENT_MATCHES);
        }

        // 3. 많이 사용한 챔피언 상위 3개 계산
        return findTopChampions(recentChampionIds, MINIMUM_CHAMPIONS_REQUIRED);
    }

    /**
     * 최근 플레이한 챔피언 ID 리스트를 Riot API에서 가져오는 메서드
     *
     * @param gameName  게임 이름
     * @param puuid     Riot PUUID
     * @return          챔피언 ID 리스트
     */
    private List<Long> fetchRecentChampionIds(String gameName, String puuid) {
        List<Long> championIds = new ArrayList<>();
        int count = INITIAL_MATCH_COUNT;

        // 최소 3개 이상의 챔피언 데이터를 가져올 때까지 반복
        while (championIds.size() < MINIMUM_CHAMPIONS_REQUIRED && count <= MAX_MATCH_COUNT) {
            List<String> matchIds = fetchMatchIds(puuid, count);
            championIds = extractChampionIdsFromMatches(matchIds, gameName);

            // 챔피언 수가 부족하면 더 많은 매칭 데이터를 가져옴
            if (championIds.size() < MINIMUM_CHAMPIONS_REQUIRED) {
                count += MATCH_INCREMENT;
            }
        }
        return championIds;
    }

    /**
     * Riot API를 호출하여 puuid에 해당하는 최근 매칭 ID를 가져오는 메서드
     *
     * @param puuid Riot PUUID
     * @param count 가져올 매칭 개수
     * @return      매칭 ID 리스트
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
     * 매칭 ID 리스트를 기반으로 특정 게임 이름에 해당하는 챔피언 ID를 추출
     *
     * @param matchIds  매칭 ID 리스트
     * @param gameName  게임 이름
     * @return          챔피언 ID 리스트
     */
    private List<Long> extractChampionIdsFromMatches(List<String> matchIds, String gameName) {
        return matchIds.stream()
                .map(matchId -> fetchChampionIdFromMatch(matchId, gameName))
                .filter(Objects::nonNull)
                .filter(championId -> championId < 1000)
                .toList();
    }

    /**
     * Riot API를 호출하여 매칭 ID로부터 특정 사용자의 챔피언 ID를 가져오는 메서드
     *
     * @param matchId   매칭 ID
     * @param gameName  소환사명
     * @return          챔피언 ID
     */
    private Long fetchChampionIdFromMatch(String matchId, String gameName) {
        String url = String.format(MATCH_INFO_URL_TEMPLATE, matchId, riotAPIKey);

        try {
            // Riot API로부터 매칭 정보를 가져오기
            RiotMatchResponse response = restTemplate.getForObject(url, RiotMatchResponse.class);

            // 매칭 정보에서 사용자의 챔피언 ID 찾기
            if (response == null || response.getInfo() == null || response.getInfo().getParticipants() == null) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }

            return response.getInfo().getParticipants().stream()
                    .filter(participant -> gameName.equals(participant.getRiotIdGameName()))
                    .map(RiotMatchResponse.ParticipantDTO::getChampionId)
                    .findFirst()
                    .orElseThrow(() -> new RiotException(ErrorCode.CHAMPION_NOT_FOUND));

        } catch (Exception e) {
            log.error("Failed to fetch champion ID for match ID: {}", matchId, e);
            throw new RiotException(ErrorCode.RIOT_MATCH_CHAMPION_NOT_FOUND);
        }
    }

    /**
     * 주어진 챔피언 ID 리스트에서 가장 많이 사용된 챔피언 상위 N개를 계산
     *
     * @param championIds   챔피언 ID 리스트
     * @param topN          가져올 상위 챔피언 개수
     * @return              많이 사용된 상위 챔피언 ID 리스트
     */
    private List<Long> findTopChampions(List<Long> championIds, int topN) {
        return championIds.stream()
                .collect(Collectors.groupingBy(championId -> championId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();
    }

}
