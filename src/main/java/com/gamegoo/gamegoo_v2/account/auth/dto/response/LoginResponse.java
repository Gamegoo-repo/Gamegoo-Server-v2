package com.gamegoo.gamegoo_v2.account.auth.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class LoginResponse {

    Long id;
    String name;
    int profileImage;
    String accessToken;
    String refreshToken;
    @Schema(ref = "#/components/schemas/BanType")
    BanType banType;
    LocalDateTime banExpireAt;
    String banMessage;
    boolean isBanned;

    public static LoginResponse of(Member member, String accessToken, String refreshToken, String banMessage) {
        return LoginResponse.builder()
                .id(member.getId())
                .name(member.getGameName())
                .profileImage(member.getProfileImage())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .banType(member.getBanType())
                .banExpireAt(member.getBanExpireAt())
                .banMessage(banMessage)
                .isBanned(member.isBanned())
                .build();
    }

}
