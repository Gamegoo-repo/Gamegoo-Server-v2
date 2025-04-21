package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.core.exception.RiotException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAuthTokenResponse;
import com.gamegoo.gamegoo_v2.external.riot.dto.response.RiotAccountIdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RiotOAuthService {

    private final RestTemplate restTemplate;

    @Value(value = "${spring.riot.redirect-uri}")
    private String redirectUri;

    @Value(value = "${spring.riot.client-id}")
    private String clientId;

    @Value(value = "${spring.riot.client-secret}")
    private String clientSecret;

    /**
     * OAuth로부터 콜백이 올 경우, GET /token으로 정보 얻기
     *
     * @param code 인증 코드
     * @return 토큰 정보
     */
    public RiotAuthTokenResponse exchangeCodeForTokens(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        String tokenUri = "https://auth.riotgames.com/token";
        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(tokenUri, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });
        Map<String, Object> body = response.getBody();

        return RiotAuthTokenResponse.builder()
                .accessToken((String) body.get("access_token"))
                .idToken((String) body.get("id_token"))
                .refreshToken((String) body.get("refresh_token"))
                .build();
    }

    /**
     * RSO Access Token으로 유저 정보 찾기
     *
     * @param accessToken 액세스 토큰
     * @return 유저정보
     */
    public RiotAccountIdResponse getSummonerInfo(String accessToken) {
        String url = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> body = response.getBody();
            return RiotAccountIdResponse.builder()
                    .id((String) body.get("id"))
                    .accountId((String) body.get("accountId"))
                    .puuid((String) body.get("puuid"))
                    .profileIconId((Integer) body.get("profileIconId"))
                    .revisionDate(((Number) body.get("revisionDate")).longValue())
                    .summonerLevel(((Number) body.get("summonerLevel")).longValue())
                    .build();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RiotException(ErrorCode.RIOT_SUMMONER_NOT_FOUND);
        } catch (Exception e) {
            throw new RiotException(ErrorCode.RIOT_API_ERROR);
        }

    }

}

