package com.gamegoo.gamegoo_v2.chat.dto.response;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.utils.DateTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ChatMessageResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long senderId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String senderName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer senderProfileImg;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String createdAt;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long timestamp;

    public static ChatMessageResponse of(Chat chat) {
        String senderName = chat.getFromMember().getBlind()
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
