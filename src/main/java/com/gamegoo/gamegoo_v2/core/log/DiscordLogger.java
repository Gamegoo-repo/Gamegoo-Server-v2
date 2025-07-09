package com.gamegoo.gamegoo_v2.core.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordLogger {

    @Value("${logging.discord.webhook}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public void send(String content) {
        Map<String, String> body = Map.of("content", content);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            log.error("디스코드 전송 실패", e);
        }
    }

    public void sendTo(String content, String webhookUrl) {
        Map<String, String> body = Map.of("content", content);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            //log.error("디스코드 전송 실패", e);
        }
    }

}
