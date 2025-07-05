package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotPuuidGameNameResponse;
import com.gamegoo.gamegoo_v2.utils.RiotApiHelper;
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
    private final RiotApiHelper riotApiHelper;

    @Value("${spring.riot.api.key}")
    private String riotAPIKey;

    private static final String RIOT_ACCOUNT_API_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s";
    private static final String RIOT_ACCOUNT_BY_PUUID_API_URL_TEMPLATE = "https://asia.api.riotgames" +
            ".com/riot/account/v1/accounts/by-puuid/%s?api_key=%s";

    /**
     * puuid로 게임 이름과 태그 얻기
     *
     * @param puuid 소환사의 puuid
     * @return RiotAccountResponse (gameName, tagLine 포함)
     */
    public RiotPuuidGameNameResponse getAccountByPuuid(String puuid) {
        String url = String.format(RIOT_ACCOUNT_BY_PUUID_API_URL_TEMPLATE, puuid, riotAPIKey);
        try {
            RiotPuuidGameNameResponse response = restTemplate.getForObject(url, RiotPuuidGameNameResponse.class);

            if (response == null || response.getGameName() == null || response.getTagLine() == null) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }

            return response;
        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return null;
        }
    }

    /**
     * puuid 얻기
     *
     * @param gameName 소환사명
     * @param tag      태그
     * @return puuid
     */
    public String getPuuid(String gameName, String tag) {
        String url = String.format(RIOT_ACCOUNT_API_URL_TEMPLATE, gameName, tag, riotAPIKey);
        try {
            RiotPuuidGameNameResponse response = restTemplate.getForObject(url, RiotPuuidGameNameResponse.class);
            if (response == null || response.getPuuid() == null) {
                throw new RiotException(ErrorCode.RIOT_NOT_FOUND);
            }
            return response.getPuuid();
        } catch (Exception e) {
            riotApiHelper.handleApiError(e);
            return null;
        }
    }

}
