package com.gamegoo.gamegoo_v2.chat.dto.response;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.utils.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatCreateResponse {

    Long senderId;
    String senderName;
    int senderProfileImg;
    String message;
    String createdAt;
    long timestamp;

    public static ChatCreateResponse of(Chat chat) {
        return ChatCreateResponse.builder()
                .senderId(chat.getFromMember().getId())
                .senderName(chat.getFromMember().getGameName())
                .senderProfileImg(chat.getFromMember().getProfileImage())
                .message(chat.getContents())
                .createdAt(DateTimeUtil.toKSTString(chat.getCreatedAt()))
                .timestamp(chat.getTimestamp())
                .build();
    }

}
