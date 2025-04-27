package com.gamegoo.gamegoo_v2.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChampionInitializerTest {

    @Test
    void testKoreanEncoding() throws IOException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("static/champion_ko.json").getInputStream();
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        
        // when
        JsonNode rootNode = mapper.readTree(reader);
        JsonNode dataNode = rootNode.path("data");
        JsonNode aatroxNode = dataNode.path("Aatrox");
        
        // then
        assertNotNull(aatroxNode);
        assertEquals("266", aatroxNode.path("key").asText());
        assertEquals("아트록스", aatroxNode.path("name").asText());
        assertEquals("Aatrox", aatroxNode.path("name_en").asText());
    }
} 