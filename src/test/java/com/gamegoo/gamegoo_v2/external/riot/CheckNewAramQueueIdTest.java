package com.gamegoo.gamegoo_v2.external.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 장진형#KR1 계정의 최근 게임에서 새로운 칼바람 모드의 queueId를 확인하는 테스트
 */
public class CheckNewAramQueueIdTest {

    // 환경변수에서 API 키 읽기
    private final String riotAPIKey = System.getenv("RIOT_API") != null ?
            System.getenv("RIOT_API") : "RGAPI-c6fe3faa-8427-41e7-913c-e43799393e40";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ACCOUNT_BY_RIOT_ID_URL = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s";
    private static final String MATCH_IDS_URL = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=%s&count=%s&api_key=%s";
    private static final String MATCH_INFO_URL = "https://asia.api.riotgames.com/lol/match/v5/matches/%s?api_key=%s";

    @Test
    public void checkNewAramQueueId() throws Exception {
        System.out.println("=".repeat(80));
        System.out.println("칼바람 새로운 모드 queueId 체크 시작");
        System.out.println("=".repeat(80));

        try {
            // 1. 계정 정보 조회
            System.out.println("\n[1단계] 장진형#KR1 계정 정보 조회 중...");
            String accountUrl = String.format(ACCOUNT_BY_RIOT_ID_URL, "장진형", "KR1", riotAPIKey);
            AccountDto account = restTemplate.getForObject(accountUrl, AccountDto.class);

            if (account == null || account.getPuuid() == null) {
                System.err.println("계정 정보를 가져올 수 없습니다.");
                return;
            }

            String puuid = account.getPuuid();
            System.out.println("PUUID 조회 완료: " + puuid);

            // 2. 최근 20경기 매치 ID 조회 (최신부터)
            System.out.println("\n[2단계] 최근 20경기 매치 ID 조회 중...");
            String matchIdsUrl = String.format(MATCH_IDS_URL, puuid, 0, 20, riotAPIKey);
            String[] matchIds = restTemplate.getForObject(matchIdsUrl, String[].class);

            if (matchIds == null || matchIds.length == 0) {
                System.err.println("매치 정보를 가져올 수 없습니다.");
                return;
            }

            System.out.println("총 " + matchIds.length + "개의 매치 발견");

            // 3. 각 매치의 queueId 수집
            System.out.println("\n[3단계] 각 매치의 게임 타입 분석 중...");
            System.out.println("-".repeat(80));

            Map<Integer, List<MatchInfo>> queueIdToMatches = new HashMap<>();
            Set<Integer> aramRelatedQueueIds = new HashSet<>();
            List<String> aramMatchDetails = new ArrayList<>();

            for (int i = 0; i < matchIds.length; i++) {
                String matchId = matchIds[i];

                try {
                    String matchInfoUrl = String.format(MATCH_INFO_URL, matchId, riotAPIKey);
                    String jsonResponse = restTemplate.getForObject(matchInfoUrl, String.class);
                    JsonNode root = objectMapper.readTree(jsonResponse);

                    JsonNode infoNode = root.get("info");
                    if (infoNode != null) {
                        int queueId = infoNode.get("queueId").asInt();
                        String gameMode = infoNode.has("gameMode") ? infoNode.get("gameMode").asText() : "N/A";
                        String gameType = infoNode.has("gameType") ? infoNode.get("gameType").asText() : "N/A";
                        int mapId = infoNode.has("mapId") ? infoNode.get("mapId").asInt() : 0;

                        MatchInfo matchInfo = new MatchInfo(matchId, queueId, gameMode, gameType, mapId);
                        queueIdToMatches.computeIfAbsent(queueId, k -> new ArrayList<>()).add(matchInfo);

                        // 모든 게임 출력 (디버깅용)
                        String championInfo = extractChampionInfo(root, "장진형");
                        System.out.println("매치 #" + (i + 1) + ": queueId=" + queueId +
                                ", mode=" + gameMode + ", type=" + gameType + ", mapId=" + mapId);
                        System.out.println("  └─ 챔피언: " + championInfo);

                        // 칼바람 관련 게임 체크 (맵 ID 12 = 칼바람)
                        if (mapId == 12 || "ARAM".equalsIgnoreCase(gameMode)) {
                            aramRelatedQueueIds.add(queueId);
                            aramMatchDetails.add("매치 #" + (i + 1) + ": " + championInfo);
                        }
                    }

                    // Rate limit 방지
                    Thread.sleep(100);

                } catch (Exception e) {
                    System.err.println("매치 " + (i + 1) + " 정보 조회 실패: " + e.getMessage());
                }
            }

            // 4. 결과 정리 및 출력
            printResults(queueIdToMatches, aramRelatedQueueIds);

        } catch (Exception e) {
            System.err.println("테스트 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractChampionInfo(JsonNode root, String gameName) {
        try {
            JsonNode participants = root.get("info").get("participants");
            if (participants != null && participants.isArray()) {
                for (JsonNode participant : participants) {
                    String riotIdGameName = participant.has("riotIdGameName") ?
                            participant.get("riotIdGameName").asText() : "";
                    if (gameName.equals(riotIdGameName)) {
                        long championId = participant.has("championId") ?
                                participant.get("championId").asLong() : 0;
                        String championName = participant.has("championName") ?
                                participant.get("championName").asText() : "Unknown";
                        boolean win = participant.has("win") && participant.get("win").asBoolean();
                        return championName + " (ID: " + championId + ") - " + (win ? "승리" : "패배");
                    }
                }
            }
        } catch (Exception e) {
            return "추출 실패: " + e.getMessage();
        }
        return "정보 없음";
    }

    private void printResults(Map<Integer, List<MatchInfo>> queueIdToMatches, Set<Integer> aramRelatedQueueIds) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("결과 요약");
        System.out.println("=".repeat(80));

        // 전체 queueId 통계
        System.out.println("\n발견된 모든 queueId:");
        queueIdToMatches.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int queueId = entry.getKey();
                    List<MatchInfo> matches = entry.getValue();
                    MatchInfo sample = matches.get(0);
                    System.out.println("  queueId " + queueId + ": " + matches.size() +
                            " 게임 (mode=" + sample.gameMode + ", type=" + sample.gameType +
                            ", mapId=" + sample.mapId + ")");
                });

        // 칼바람 관련 queueId 분석
        System.out.println("\n칼바람 관련 queueId 분석:");
        System.out.println("  기존 칼바람 queueId: 450");

        if (aramRelatedQueueIds.isEmpty()) {
            System.out.println("  최근 30경기에서 칼바람 게임을 찾을 수 없습니다.");
        } else {
            System.out.println("  발견된 칼바람 queueId: " + aramRelatedQueueIds);

            if (aramRelatedQueueIds.contains(450)) {
                System.out.println("  ✅ 기존 칼바람 모드 (450) 발견됨");
            }

            Set<Integer> newQueueIds = new HashSet<>(aramRelatedQueueIds);
            newQueueIds.remove(450);

            if (!newQueueIds.isEmpty()) {
                System.out.println("\n⚠️ 새로운 칼바람 queueId 발견: " + newQueueIds);
                System.out.println("⚠️ RiotRecordService.java 수정 필요 여부 확인:");
                System.out.println("     - 현재 코드는 queueId == 450만 칼바람으로 인식");
                System.out.println("     - 새로운 queueId " + newQueueIds + "도 칼바람으로 처리해야 하는지 검토 필요");
                System.out.println("\n수정 제안:");
                System.out.println("   Option 1) QUEUE_ID_ARAM 상수를 배열로 변경");
                System.out.println("   Option 2) isAramQueue(int queueId) 메서드 추가");
                System.out.println("   Option 3) 새 모드가 기존과 다르다면 별도 상수 추가");
            } else {
                System.out.println("  ✅ 새로운 칼바람 queueId 없음 - 기존 450만 사용 중");
                System.out.println("  ✅ RiotRecordService.java 수정 불필요");
            }
        }

        System.out.println("\n" + "=".repeat(80));
    }

    // DTO 클래스들
    public static class AccountDto {
        private String puuid;
        private String gameName;
        private String tagLine;

        public String getPuuid() { return puuid; }
        public void setPuuid(String puuid) { this.puuid = puuid; }
        public String getGameName() { return gameName; }
        public void setGameName(String gameName) { this.gameName = gameName; }
        public String getTagLine() { return tagLine; }
        public void setTagLine(String tagLine) { this.tagLine = tagLine; }
    }

    private static class MatchInfo {
        String matchId;
        int queueId;
        String gameMode;
        String gameType;
        int mapId;

        MatchInfo(String matchId, int queueId, String gameMode, String gameType, int mapId) {
            this.matchId = matchId;
            this.queueId = queueId;
            this.gameMode = gameMode;
            this.gameType = gameType;
            this.mapId = mapId;
        }
    }
}
