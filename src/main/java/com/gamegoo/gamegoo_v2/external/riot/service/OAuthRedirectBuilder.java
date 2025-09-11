package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuthRedirectBuilder {

    public String buildLoginRedirectUrl(Member member, String state, String frontUrl, String accessToken,
                                        String refreshToken) {
        String encodedGameName = URLEncoder.encode(member.getGameName(), StandardCharsets.UTF_8);

        return String.format("%s?status=LOGIN_SUCCESS&accessToken=%s&refreshToken=%s&name=%s&profileImage=%s&id=%s" +
                        "&state=%s",
                frontUrl,
                accessToken,
                refreshToken,
                encodedGameName,
                member.getProfileImage(),
                member.getId(),
                state);
    }

    public String buildJoinRedirectUrl(String frontUrl, String state, String puuid) {
        String encodedPuuid = URLEncoder.encode(puuid, StandardCharsets.UTF_8);
        return String.format("%s?status=NEED_SIGNUP&puuid=%s&state=%s", frontUrl, encodedPuuid, state);
    }

}
