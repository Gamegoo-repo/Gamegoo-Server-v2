package com.gamegoo.gamegoo_v2.social.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendRequestResponse {

    Long targetMemberId;
    String message;

    public static FriendRequestResponse of(Long targetMemberId, String message) {
        return FriendRequestResponse.builder()
                .targetMemberId(targetMemberId)
                .message(message)
                .build();
    }

}
