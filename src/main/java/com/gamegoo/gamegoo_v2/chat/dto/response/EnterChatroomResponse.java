package com.gamegoo.gamegoo_v2.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnterChatroomResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String uuid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long memberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String gameName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int memberProfileImg;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean friend;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean blocked;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean blind;
    Long friendRequestMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    SystemFlagResponse system;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    ChatMessageListResponse chatMessageListResponse;

    @Getter
    @Builder
    public static class SystemFlagResponse {

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int flag;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long boardId;

        public static SystemFlagResponse of(int flag, Long boardId) {
            return SystemFlagResponse.builder()
                    .flag(flag)
                    .boardId(boardId)
                    .build();
        }

    }

}
