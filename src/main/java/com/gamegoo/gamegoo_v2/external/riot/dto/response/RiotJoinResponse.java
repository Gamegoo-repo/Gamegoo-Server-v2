package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotJoinResponse {

    private final Long id;
    private final String name;
    private final String tag;
    private final int profileImage;
    private final String accessToken;
    private final String refreshToken;

    public static RiotJoinResponse of(Member member, String accessToken, String refreshToken) {
        return RiotJoinResponse.builder()
                .id(member.getId())
                .name(member.getGameName())
                .tag(member.getTag())
                .profileImage(member.getProfileImage())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
