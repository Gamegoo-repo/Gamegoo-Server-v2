package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.dto.RiotAuthResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.RiotSummonerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiotAuthService {

    private final RestTemplate restTemplate;

    @Value("${spring.riot.api.key}")
    private String riotAPIKey;

    private static final String RIOT_ACCOUNT_API_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s";
    private static final String RIOT_SUMMONER_API_URL_TEMPLATE = "https://kr.api.riotgames" +
            ".com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s";

    /**
     * puuid 얻기
     *
     * @param gameName  소환사명
     * @param tag       태그
     * @return          puuid
     */
    public String getPuuid(String gameName, String tag) {
        String url = String.format(RIOT_ACCOUNT_API_URL_TEMPLATE, gameName, tag, riotAPIKey);
        try {
            RiotAuthResponse response = restTemplate.getForObject(url, RiotAuthResponse.class);

            if (response == null || response.getPuuid() == null) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }

            return response.getPuuid();
        } catch (Exception e) {
            throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
        }
    }

    /**
     * 소환사아이디 얻기
     *
     * @param puuid puuid
     * @return      summonerId
     */
    public String getSummonerId(String puuid) {
        String url = String.format(RIOT_SUMMONER_API_URL_TEMPLATE, puuid, riotAPIKey);
        try {
            RiotSummonerResponse summonerResponse = restTemplate.getForObject(url, RiotSummonerResponse.class);

            if (summonerResponse == null || summonerResponse.getId() == null) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }

            return summonerResponse.getId();
        } catch (Exception e) {
            throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
        }
    }

}
