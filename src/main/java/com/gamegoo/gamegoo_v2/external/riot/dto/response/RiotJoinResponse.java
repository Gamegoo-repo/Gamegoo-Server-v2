package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotJoinResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String name;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String tag;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final int profileImage;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String accessToken;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
