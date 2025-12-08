package com.gamegoo.gamegoo_v2.account.auth.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RejoinResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final Long id;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String gameName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String tag;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final int profileImage;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String accessToken;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String refreshToken;

    private final BanType banType;
    private final String banMessage;
    private LocalDateTime banExpireAt;

    public static RejoinResponse of(Member member, String accessToken, String refreshToken, String banMessage) {
        return RejoinResponse.builder()
                .id(member.getId())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .profileImage(member.getProfileImage())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .banType(member.getBanType())
                .banExpireAt(member.getBanExpireAt())
                .banMessage(banMessage)
                .build();
    }

}
