package com.gamegoo.gamegoo_v2.utils;

import java.security.SecureRandom;

public class RandomCodeGeneratorUtil {

    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARACTERS = UPPER_CASE + LOWER_CASE + DIGITS;
    private static final SecureRandom random = new SecureRandom();
    private static final int EMAIL_CODE_LENGTH = 8;

    /**
     * 랜덤 코드 만드는 유틸
     *
     * @return 랜덤코드
     */
    public static String generateEmailRandomCode() {
        StringBuilder code = new StringBuilder(EMAIL_CODE_LENGTH);

        code.append(UPPER_CASE.charAt(random.nextInt(UPPER_CASE.length())));
        code.append(LOWER_CASE.charAt(random.nextInt(LOWER_CASE.length())));
        code.append(DIGITS.charAt(random.nextInt(DIGITS.length())));

        for (int i = 3; i < EMAIL_CODE_LENGTH; i++) {
            int index = random.nextInt(UPPER_CASE.length() + DIGITS.length());
            code.append(ALL_CHARACTERS.charAt(index));
        }
        return shuffleString(code.toString());
    }

    /**
     * 문자열 내 순서 섞는 매서드
     *
     * @param input 문자열
     * @return      순서가 뒤바뀐 문자열
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();

        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }

        return new String(characters);
    }

}
