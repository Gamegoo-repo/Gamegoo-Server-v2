package com.gamegoo.gamegoo_v2.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class ChatMessageListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<ChatMessageResponse> chatMessageList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int listSize;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean hasNext;
    Long nextCursor;

}
