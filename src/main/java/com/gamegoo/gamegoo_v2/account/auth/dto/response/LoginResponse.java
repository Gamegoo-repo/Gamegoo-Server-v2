package com.gamegoo.gamegoo_v2.account.auth.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    Long id;
    String name;
    int profileImage;
    String accessToken;
    String refreshToken;

    public static LoginResponse of(Member member, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .id(member.getId())
                .name(member.getGameName())
                .profileImage(member.getProfileImage())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
