package com.gamegoo.gamegoo_v2.chat.dto.response;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.utils.DateTimeUtil;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ChatMessageResponse {

    Long senderId;
    String senderName;
    Integer senderProfileImg;
    String message;
    String createdAt;
    Long timestamp;

    public static ChatMessageResponse of(Chat chat) {
        String senderName = chat.getFromMember().isBlind()
                ? "(탈퇴한 사용자)"
                : chat.getFromMember().getGameName();

        return ChatMessageResponse.builder()
                .senderId(chat.getFromMember().getId())
                .senderName(senderName)
                .senderProfileImg(chat.getFromMember().getProfileImage())
                .message(chat.getContents())
                .createdAt(DateTimeUtil.toKSTString(chat.getCreatedAt()))
                .timestamp(chat.getTimestamp())
                .build();
    }

}
