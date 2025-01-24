package com.gamegoo.gamegoo_v2.service.board;

import com.gamegoo.gamegoo_v2.content.board.service.ProfanityCheckService;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class ProfanityCheckServiceTest {

    @Autowired
    private ProfanityCheckService profanityCheckService;

    @Test
    @DisplayName("정상 텍스트(금지어 없음) 테스트")
    void noBadWord() {

        String text = "안녕하세요, 반갑습니다.";
        assertDoesNotThrow(() -> profanityCheckService.validateProfanity(text));
    }

    @Test
    @DisplayName("금지어 포함 시 예외 발생 테스트")
    void hasBadWord() {

        String text = "존1나 ㅈ같네 ㅋㅋ.";

        assertThatThrownBy(() -> profanityCheckService.validateProfanity(text))
                .isInstanceOf(BoardException.class)
                .hasMessage(ErrorCode.BOARD_Forbidden_WORD.getMessage());
    }

}



