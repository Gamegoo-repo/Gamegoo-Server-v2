package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.core.config.RiotOAuthProperties;
import com.gamegoo.gamegoo_v2.external.riot.dto.RiotAuthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RiotOAuthService {

    private final RestTemplate restTemplate;
    private final RiotOAuthProperties properties;

    public RiotAuthTokenResponse exchangeCodeForTokens(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", properties.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(properties.getTokenUrl(), HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });
        Map<String, Object> body = response.getBody();

        return RiotAuthTokenResponse.builder()
                .accessToken((String) body.get("access_token"))
                .idToken((String) body.get("id_token"))
                .refreshToken((String) body.get("refresh_token"))
                .build();
    }
}

