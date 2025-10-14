package com.gamegoo.gamegoo_v2.chat.dto.response;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.utils.DateTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatCreateResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long senderId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String senderName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int senderProfileImg;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String createdAt;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
