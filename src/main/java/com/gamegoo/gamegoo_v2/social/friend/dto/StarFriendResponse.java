package com.gamegoo.gamegoo_v2.social.friend.dto;

import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StarFriendResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long friendMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;

    public static StarFriendResponse of(Friend friend) {
        String message;
        if (friend.isLiked()) {
            message = "친구 즐겨찾기 설정 성공";
        } else {
            message = "친구 즐겨찾기 해제 성공";
        }

        return StarFriendResponse.builder()
                .friendMemberId(friend.getToMember().getId())
                .message(message)
                .build();
    }

}
