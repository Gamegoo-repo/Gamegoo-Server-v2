package com.gamegoo.gamegoo_v2.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    /**
     * 비밀번호 암호화
     * @param rawPassword   암호화 전 비밀번호
     * @return              암호화 후 비밀번호
     */
    public static String encodePassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }

    /**
     * 비밀번호 검증
     * @param rawPassword       암호화 전 비교할 비밀번호
     * @param encodedPassword   암호화 된 원본 비밀번호
     * @return                  일치 여부
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }

}

