package com.gamegoo.gamegoo_v2.chat.dto.data;

import java.time.LocalDateTime;

public interface ChatroomSummaryDTO {

    Long getChatroomId();

    String getChatroomUuid();

    Integer getUnreadCnt();

    String getLastChat();

    LocalDateTime getLastChatAt();

    Long getLastChatTimestamp();

    Long getTargetMemberId();

    String getTargetMemberName();

    Integer getTargetMemberImg();

    Boolean getBlind();

}
