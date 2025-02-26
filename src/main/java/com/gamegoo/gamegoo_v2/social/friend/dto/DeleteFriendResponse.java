package com.gamegoo.gamegoo_v2.social.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteFriendResponse {

    Long targetMemberId;
    String message;

    public static DeleteFriendResponse of(Long targetMemberId) {
        return DeleteFriendResponse.builder()
                .targetMemberId(targetMemberId)
                .message("친구 삭제 성공")
                .build();
    }

}
