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
                                        String refreshToken, String banMessage) {
        String encodedGameName = URLEncoder.encode(member.getGameName(), StandardCharsets.UTF_8);
        String encodedTag = URLEncoder.encode(member.getTag(), StandardCharsets.UTF_8);
        String encodedAccessToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String encodedRefreshToken = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        String encodedBanType = URLEncoder.encode(String.valueOf(member.getBanType()), StandardCharsets.UTF_8);
        String encodedBanExpireAt = URLEncoder.encode(String.valueOf(member.getBanExpireAt()), StandardCharsets.UTF_8);
        String encodedBanMessage = "";
        if (banMessage != null) {
            encodedBanMessage = URLEncoder.encode(banMessage, StandardCharsets.UTF_8);
        }
        String encodedIsBanned = URLEncoder.encode(String.valueOf(member.isBanned()), StandardCharsets.UTF_8);

        return String.format(
                "%s?status=LOGIN_SUCCESS&accessToken=%s&refreshToken=%s&name=%s&tag=%s&profileImage=%s&id=%s" +
                        "&state=%s&banType=%s&banExpireAt=%s&banMessage=%s&isBanned=%s",
                frontUrl,
                encodedAccessToken,
                encodedRefreshToken,
                encodedGameName,
                encodedTag,
                member.getProfileImage(),
                member.getId(),
                state,
                encodedBanType,
                encodedBanExpireAt,
                encodedBanMessage,
                encodedIsBanned
        );
    }

    public String buildJoinRedirectUrl(String frontUrl, String state, String puuid) {
        String encodedPuuid = URLEncoder.encode(puuid, StandardCharsets.UTF_8);

        return String.format("%s?status=NEED_SIGNUP&puuid=%s&state=%s", frontUrl, encodedPuuid, state);
    }

}

