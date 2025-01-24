package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfanityCheckService {

    private final List<String> blockedWords = new ArrayList<>();

    /**
     * 빈 생성 후 초기화 시점에
     * resources 폴더 내 금지어 파일을 읽어 blockedWords 리스트를 구성
     */
    @PostConstruct
    public void loadBlockedWords() {
        try {
            ClassPathResource resource = new ClassPathResource("bad-words.txt");
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    blockedWords.add(line.trim().toLowerCase());
                }
            }

        } catch (Exception e) {
            throw new BoardException(ErrorCode.BOARD_FORBIDDEN_WORD_LOAD_FAILED);
        }
    }

    /**
     * 게시글/댓글 등 특정 text에 대해 금지어 검사
     * 금지어 발견 시 예외(BoardException) 발생
     */
    public void validateProfanity(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String lowered = text.toLowerCase();
        for (String badWord : blockedWords) {
            if (lowered.contains(badWord)) {
                throw new BoardException(ErrorCode.BOARD_Forbidden_WORD);
            }
        }
    }

}
