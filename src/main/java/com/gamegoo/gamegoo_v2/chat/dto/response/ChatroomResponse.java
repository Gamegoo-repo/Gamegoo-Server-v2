package com.gamegoo.gamegoo_v2.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatroomResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long chatroomId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String uuid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long targetMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int targetMemberImg;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String targetMemberName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean friend;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean blocked;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean blind;
    Long friendRequestMemberId;
    String lastMsg;
    String lastMsgAt;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int notReadMsgCnt;
    Long lastMsgTimestamp;

}
