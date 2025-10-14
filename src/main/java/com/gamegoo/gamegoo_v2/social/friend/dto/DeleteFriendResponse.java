package com.gamegoo.gamegoo_v2.social.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteFriendResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long targetMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;

    public static DeleteFriendResponse of(Long targetMemberId) {
        return DeleteFriendResponse.builder()
                .targetMemberId(targetMemberId)
                .message("친구 삭제 성공")
                .build();
    }

}
