package com.gamegoo.gamegoo_v2.riot.service;

import com.gamegoo.gamegoo_v2.exception.RiotException;
import com.gamegoo.gamegoo_v2.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.member.domain.Tier;
import com.gamegoo.gamegoo_v2.riot.dto.RiotInfoResponse;
import com.gamegoo.gamegoo_v2.riot.dto.TierDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RiotInfoService {

    private final RestTemplate restTemplate;

    @Value("${spring.riot.api.key}")
    private String riotAPIKey;

    private static final String RIOT_LEAGUE_API_URL_TEMPLATE = "https://kr.api.riotgames" +
            ".com/lol/league/v4/entries/by-summoner/%s?api_key=%s";

    private static final Map<String, Integer> romanToIntMap = Map.of(
            "I", 1, "II", 2, "III", 3, "IV", 4
    );

    public TierDetails getTierWinrateRank(String encryptedSummonerId) {
        String url = String.format(RIOT_LEAGUE_API_URL_TEMPLATE, encryptedSummonerId, riotAPIKey);

        try {
            RiotInfoResponse[] responses = restTemplate.getForObject(url, RiotInfoResponse[].class);

            if (responses != null) {
                for (RiotInfoResponse response : responses) {
                    if ("RANKED_SOLO_5x5".equals(response.getQueueType())) {
                        int totalGames = response.getWins() + response.getLosses();
                        double winRate = Math.round((double) response.getWins() / totalGames * 1000) / 10.0;
                        Tier tier = Tier.valueOf(response.getTier().toUpperCase());
                        int rank = romanToIntMap.get(response.getRank());

                        return new TierDetails(tier, winRate, rank, totalGames);
                    }
                }
            }
        } catch (Exception e) {
            log.error("RIOT API INTERNAL ERROR: ", e);
            throw new RiotException(ErrorCode.RIOT_ERROR);
        }

        return new TierDetails(Tier.UNRANKED, 0, 0, 0);

    }

}
