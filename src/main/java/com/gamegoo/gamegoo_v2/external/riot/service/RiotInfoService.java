package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.external.riot.dto.RiotInfoResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.TierDetails;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.utils.RiotApiHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RiotInfoService {

    private final RestTemplate restTemplate;
    private final RiotApiHelper riotApiHelper;

    @Value("${spring.riot.api.key}")
    private String riotAPIKey;

    private static final String RIOT_LEAGUE_API_URL_TEMPLATE = "https://kr.api.riotgames" +
            ".com/lol/league/v4/entries/by-summoner/%s?api_key=%s";
    private static final String RIOT_SOLO_QUEUE_TYPE = "RANKED_SOLO_5x5";
    private static final String RIOT_FREE_QUEUE_TYPE = "RANKED_FLEX_SR";
    private static final Map<String, Integer> romanToIntMap = Map.of(
            "I", 1, "II", 2, "III", 3, "IV", 4
    );

    /**
     * 티어, 랭크, 승률 조회
     *
     * @param encryptedSummonerId 암호화된 소환사 id
     * @return 소환사 정보
     */
    public List<TierDetails> getTierWinrateRank(String encryptedSummonerId) {
        // 티어 정보
        List<TierDetails> tierDetails = new ArrayList<>();

        // riot API 호출
        String url = String.format(RIOT_LEAGUE_API_URL_TEMPLATE, encryptedSummonerId, riotAPIKey);
        try {
            RiotInfoResponse[] responses = restTemplate.getForObject(url, RiotInfoResponse[].class);
            if (responses != null) {
                for (RiotInfoResponse response : responses) {
                    if (RIOT_SOLO_QUEUE_TYPE.equals(response.getQueueType())) {
                        tierDetails.add(calculateTierDetails(response, GameMode.SOLO));
                    }
                    if (RIOT_FREE_QUEUE_TYPE.equals(response.getQueueType())) {
                        tierDetails.add(calculateTierDetails(response, GameMode.FREE));
                    }
                }
            }
        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return null;
        }

        return tierDetails;
    }

    /**
     * riot 정보 가공
     *
     * @param response riot resposne
     * @return 소환사 정보
     */
    private static TierDetails calculateTierDetails(RiotInfoResponse response, GameMode gameMode) {
        int totalGames = response.getWins() + response.getLosses();
        double winRate = Math.round((double) response.getWins() / totalGames * 1000) / 10.0;
        Tier tier = Tier.valueOf(response.getTier().toUpperCase());
        int rank = romanToIntMap.get(response.getRank());
        return new TierDetails(gameMode, tier, winRate, rank, totalGames);
    }

}
