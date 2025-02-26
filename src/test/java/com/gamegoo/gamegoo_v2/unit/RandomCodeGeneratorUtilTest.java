package com.gamegoo.gamegoo_v2.unit;

import com.gamegoo.gamegoo_v2.utils.RandomCodeGeneratorUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomCodeGeneratorUtilTest {

    @DisplayName("인증 코드 랜덤 생성")
    @Test
    void testGenerateEmailRandomCode_LengthAndFormat() {
        // 랜덤 코드 생성
        String randomCode = RandomCodeGeneratorUtil.generateEmailRandomCode();

        // 랜덤 코드 길이 검증
        assertEquals(8, randomCode.length(), "생성된 랜덤코드의 길이가 8이어야한다.");

        // 랜덤 코드 최소 포함 글자 검증
        assertTrue(randomCode.matches(".*[A-Z].*"), "생성된 랜덤코드는 대문자를 하나 반드시 포함해야한다.");
        assertTrue(randomCode.matches(".*[a-z].*"), "생성된 랜덤코드는 소문자를 하나 반드시 포함해야한다.");
        assertTrue(randomCode.matches(".*[0-9].*"), "생성된 랜덤코드는 숫자를 하나 반드시 포함해야한다.");
    }

}
