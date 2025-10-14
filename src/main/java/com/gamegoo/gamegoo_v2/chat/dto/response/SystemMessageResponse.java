package com.gamegoo.gamegoo_v2.chat.dto.response;

import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.utils.DateTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SystemMessageResponse extends ChatMessageResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer systemType;
    Long boardId;

    public static SystemMessageResponse of(Chat chat) {
        Long boardId = chat.getSourceBoard() != null
                ? chat.getSourceBoard().getId()
                : null;

        return SystemMessageResponse.builder()
                .senderId(chat.getFromMember().getId())
                .senderName(null)
                .senderProfileImg(null)
                .message(chat.getContents())
                .createdAt(DateTimeUtil.toKSTString(chat.getCreatedAt()))
                .timestamp(chat.getTimestamp())
                .boardId(boardId)
                .systemType(chat.getSystemType())
                .build();
    }

}
