package com.gamegoo.gamegoo_v2.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.gamegoo_v2.game.domain.Champion;
import com.gamegoo.gamegoo_v2.game.repository.ChampionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ChampionInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final ChampionRepository championRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isCreateMode(event)) {
            try {
                initializeChampions();
            } catch (IOException e) {
                System.out.println(e.getClass());
            }
        }
    }

    private boolean isCreateMode(ApplicationReadyEvent event) {
        // jpa.hibernate.ddl-auto 값이 create인지 확인
        String ddlAuto = event.getApplicationContext().getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        return "create".equalsIgnoreCase(ddlAuto);
    }

    private void initializeChampions() throws IOException {
        // JSON 파일을 읽어 파싱합니다.
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("static/champion_ko.json").getInputStream();
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonNode rootNode = mapper.readTree(reader);
        JsonNode dataNode = rootNode.path("data");

        for (JsonNode championNode : dataNode) {
            Long key = championNode.path("key").asLong();
            String name = championNode.path("name").asText();

            Champion champion = Champion.create(key, name);

            championRepository.save(champion);
        }
    }

}
