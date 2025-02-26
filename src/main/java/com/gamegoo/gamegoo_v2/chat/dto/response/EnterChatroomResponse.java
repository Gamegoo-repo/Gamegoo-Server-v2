package com.gamegoo.gamegoo_v2.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnterChatroomResponse {

    String uuid;
    Long memberId;
    String gameName;
    int memberProfileImg;
    boolean friend;
    boolean blocked;
    boolean blind;
    Long friendRequestMemberId;
    SystemFlagResponse system;
    ChatMessageListResponse chatMessageListResponse;

    @Getter
    @Builder
    public static class SystemFlagResponse {

        int flag;
        Long boardId;

        public static SystemFlagResponse of(int flag, Long boardId) {
            return SystemFlagResponse.builder()
                    .flag(flag)
                    .boardId(boardId)
                    .build();
        }

    }

}
