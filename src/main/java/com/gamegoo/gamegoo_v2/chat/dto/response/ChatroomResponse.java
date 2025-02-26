package com.gamegoo.gamegoo_v2.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatroomResponse {

    Long chatroomId;
    String uuid;
    Long targetMemberId;
    int targetMemberImg;
    String targetMemberName;
    boolean friend;
    boolean blocked;
    boolean blind;
    Long friendRequestMemberId;
    String lastMsg;
    String lastMsgAt;
    int notReadMsgCnt;
    Long lastMsgTimestamp;

}
