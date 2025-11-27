package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.domain.GameMatch;
import com.gamegoo.gamegoo_v2.external.riot.dto.GameMatchData;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.AllModeStatsResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.Recent30GameStatsResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotMatchResponse;
import com.gamegoo.gamegoo_v2.external.riot.repository.GameMatchRepository;
import com.gamegoo.gamegoo_v2.external.riot.util.AllModeStatsCollector;
import com.gamegoo.gamegoo_v2.utils.ChampionIdStore;
import com.gamegoo.gamegoo_v2.utils.RiotApiHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final GameMatchRepository gameMatchRepository;
    private final GameMatchSaver gameMatchSaver;

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


    /**
     * Riot API: 최근 선호 챔피언 4개 리스트 조회
     *
     * @param puuid    Riot PUUID
     * @return 선호 챔피언 ID 리스트
     */
    public List<ChampionStats> getPreferChampionfromMatch(String puuid) {
        // 1. 최근 플레이한 챔피언 ID 리스트 가져오기
        Map<Long, ChampionStats> championStatsMap = fetchRecentChampionStats(puuid);

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
     * @param puuid    Riot PUUID
     * @param targetQueueId 대상 큐 ID (420: 솔로, 440: 자유, 450: 칼바람)
     * @return 선호 챔피언 ID 리스트
     */
    public List<ChampionStats> getPreferChampionFromMatchByQueueId(String puuid, int targetQueueId) {
        // 1. 특정 큐 ID의 최근 플레이한 챔피언 통계 가져오기
        Map<Long, ChampionStats> championStatsMap = fetchRecentChampionStatsByQueueId(puuid, targetQueueId);

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
     * @param puuid    Riot PUUID
     * @return 챔피언 ID 리스트
     */
    private Map<Long, ChampionStats> fetchRecentChampionStats(String puuid) {
        Map<Long, ChampionStats> championStatsMap = new HashMap<>();

        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        for (String matchId : matchIds) {
            Optional<ChampionStats> championStatsOpt = fetchChampionStatsFromMatch(matchId, puuid);
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
     * @param puuid    Riot PUUID
     * @param targetQueueId 대상 큐 ID
     * @return 큐 ID별 챔피언 통계 맵
     */
    private Map<Long, ChampionStats> fetchRecentChampionStatsByQueueId(String puuid, int targetQueueId) {
        Map<Long, ChampionStats> championStatsMap = new HashMap<>();

        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        for (String matchId : matchIds) {
            Optional<ChampionStats> championStatsOpt = fetchChampionStatsFromMatch(matchId, puuid);
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
        log.info("[Riot API 호출] fetchMatchIds - puuid: {}, start: {}, count: {}", puuid, start, count);
        try {
            // Riot API로부터 매칭 ID 리스트 가져오기
            String[] matchIds = restTemplate.getForObject(url, String[].class);
            log.info("[Riot API 응답] fetchMatchIds - 조회된 매치 수: {}", matchIds != null ? matchIds.length : 0);
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
     * @param puuid    사용자의 Riot PUUID
     * @return 챔피언 ID
     */
    private Optional<ChampionStats> fetchChampionStatsFromMatch(String matchId, String puuid) {
        String url = String.format(MATCH_INFO_URL_TEMPLATE, matchId, riotAPIKey);
        log.info("[Riot API 호출] fetchChampionStatsFromMatch - matchId: {}", matchId);

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
                    .filter(participant -> puuid.equals(participant.getPuuid()))
                    .findFirst()
                    .map(participant -> ChampionStats.from(participant, queueId, finalGameDuration));

        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return Optional.empty();
        }
    }

    public Recent30GameStatsResponse getRecent30GameStatsByQueueId(String puuid, int targetQueueId) {
        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        return calculateStatsFromMatches(matchIds, puuid, stats -> stats.getQueueId() == targetQueueId);
    }

    public Recent30GameStatsResponse getRecent30GameStats(String puuid) {
        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        return calculateStatsFromMatches(matchIds, puuid,
            stats -> stats.getQueueId() == QUEUE_ID_SOLO_RANK || stats.getQueueId() == QUEUE_ID_FREE_RANK);
    }

    /**
     * 매치 목록에서 필터 조건에 맞는 게임들의 통계를 계산
     */
    private Recent30GameStatsResponse calculateStatsFromMatches(List<String> matchIds, String puuid,
                                                                 java.util.function.Predicate<ChampionStats> filter) {
        List<ChampionStats> filteredStats = matchIds.stream()
                .map(matchId -> fetchChampionStatsFromMatch(matchId, puuid))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(filter)
                .collect(Collectors.toList());

        return Recent30GameStatsResponse.fromChampionStats(filteredStats);
    }


    /**
     * 최적화된 메서드: 한 번의 API 호출로 모든 모드별 통계를 계산
     */
    public AllModeStatsResponse getAllModeStatsOptimized(String puuid) {
        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        AllModeStatsCollector collector = new AllModeStatsCollector();

        // 한 번의 루프로 모든 매치 데이터 처리
        for (String matchId : matchIds) {
            Optional<ChampionStats> championStatsOpt = fetchChampionStatsFromMatch(matchId, puuid);
            championStatsOpt.ifPresent(collector::processChampionStats);
        }

        return collector.buildResponse();
    }

    // ===========================
    // 증분 업데이트 (DB 캐싱) 로직
    // ===========================

    /**
     * 신규 매치만 조회하여 DB에 저장 (증분 업데이트)
     * DB에 이미 있는 매치를 만나면 이후 매치는 스킵
     *
     * @param member 사용자
     * @param gameName 게임 닉네임
     * @param puuid Riot PUUID
     * @return 저장된 신규 매치 개수
     */
    @Transactional
    public int fetchAndSaveNewMatches(Member member, String gameName, String puuid) {
        // 1. Riot API로 최신 30개 matchId 조회
        List<String> matchIds = Optional.ofNullable(fetchMatchIds(puuid, 0, INITIAL_MATCH_COUNT))
                .orElseGet(Collections::emptyList);

        int savedCount = 0;

        // 2. matchId를 순회하며 증분 업데이트
        for (String matchId : matchIds) {
            // 2-1. DB에 이미 저장된 매치인지 확인
            if (gameMatchRepository.existsByMemberAndMatchId(member, matchId)) {
                // 이미 저장되어 있음 → 이후 매치도 모두 저장되어 있음 → BREAK
                log.info("[증분 업데이트] matchId={} 이미 저장되어 있음. API 호출 중단. 총 {}개 신규 매치 저장됨", matchId, savedCount);
                break;
            }

            // 2-2. 신규 매치 → Riot API 호출하여 상세 정보 조회
            Optional<GameMatchData> matchDataOpt = fetchGameMatchData(matchId, puuid);
            if (matchDataOpt.isEmpty()) {
                log.warn("[증분 업데이트] matchId={} 조회 실패. 스킵", matchId);
                continue;
            }

            // 2-3. GameMatch 엔티티 생성 및 저장
            GameMatchData matchData = matchDataOpt.get();
            GameMatch gameMatch = matchData.toEntity(member, matchId, puuid, gameName);

            try {
                gameMatchSaver.saveGameMatch(gameMatch);
                savedCount++;
                log.debug("[증분 업데이트] matchId={} 저장 완료", matchId);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.info("[증분 업데이트] matchId={} 중복 저장 시도. 스킵", matchId);
            }
        }

        log.info("[증분 업데이트] 총 {}개의 신규 매치 저장됨", savedCount);
        return savedCount;
    }

    /**
     * 단일 매치 상세 정보 조회
     *
     * @param matchId 매치 ID
     * @param puuid 사용자 PUUID
     * @return GameMatch 데이터
     */
    private Optional<GameMatchData> fetchGameMatchData(String matchId, String puuid) {
        String url = String.format(MATCH_INFO_URL_TEMPLATE, matchId, riotAPIKey);
        log.info("[Riot API 호출] fetchGameMatchData - matchId: {} (증분 업데이트)", matchId);

        try {
            RiotMatchResponse response = restTemplate.getForObject(url, RiotMatchResponse.class);
            if (response == null || response.getInfo() == null || response.getInfo().getParticipants() == null) {
                return Optional.empty();
            }

            int queueId = response.getInfo().getQueueId();
            // 솔로 랭크, 자유 랭크, 칼바람만 저장
            if (queueId != QUEUE_ID_SOLO_RANK && queueId != QUEUE_ID_FREE_RANK && queueId != QUEUE_ID_ARAM) {
                return Optional.empty();
            }

            int gameDuration = response.getInfo().getGameDuration();
            if (gameDuration <= 0) {
                gameDuration = 1800; // 기본값 30분
            }

            // 게임 시작 시각 변환 (Unix timestamp → LocalDateTime)
            long gameCreation = response.getInfo().getGameCreation();
            LocalDateTime gameStartedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(gameCreation),
                    ZoneId.of("UTC")
            );

            final int finalGameDuration = gameDuration;

            return response.getInfo().getParticipants().stream()
                    .filter(participant -> puuid.equals(participant.getPuuid()))
                    .findFirst()
                    .map(participant -> GameMatchData.from(participant, queueId, finalGameDuration, gameStartedAt));

        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return Optional.empty();
        }
    }

    /**
     * DB에서 최근 30개 매치 조회 후 모든 모드별 통계 계산
     *
     * 기존 getAllModeStatsOptimized와 동일한 응답 형식
     * Riot API 아닌 DB 조회 사용
     *
     * @param member 사용자
     * @return 모든 모드별 통계
     */
    public AllModeStatsResponse getAllModeStatsFromDB(Member member) {
        // 1. DB에서 최근 30개 매치 조회
        List<GameMatch> recentMatches = gameMatchRepository.findTop30ByMemberOrderByGameStartedAtDesc(member);

        // 2. GameMatch → ChampionStats 변환
        List<ChampionStats> championStatsList = recentMatches.stream()
                .map(GameMatch:: toChampionStats)
                .collect(Collectors.toList());

        // 3. 기존 collector 로직 재사용
        AllModeStatsCollector collector = new AllModeStatsCollector();
        championStatsList.forEach(collector::processChampionStats);

        return collector.buildResponse();
    }

}
