package com.gamegoo.gamegoo_v2.social.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendRequestResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long targetMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;

    public static FriendRequestResponse of(Long targetMemberId, String message) {
        return FriendRequestResponse.builder()
                .targetMemberId(targetMemberId)
                .message(message)
                .build();
    }

}
