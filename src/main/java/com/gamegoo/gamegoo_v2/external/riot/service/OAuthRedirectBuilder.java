package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuthRedirectBuilder {

    public String buildRedirectUrl(Member member, String state, String frontUrl, String accessToken,
                                   String refreshToken) {
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        String encodedGameName = URLEncoder.encode(member.getGameName(), StandardCharsets.UTF_8);

        return String.format("%s/riot/callback?accessToken=%s&refreshToken=%s&name=%s&profileImage=%s&id=%s&state=%s",
                frontUrl,
                accessToken,
                refreshToken,
                encodedGameName,
                member.getProfileImage(),
                member.getId(),
                encodedState);
    }

}
