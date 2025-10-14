package com.gamegoo.gamegoo_v2.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatroomListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<ChatroomResponse> chatroomResponseList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int listSize;

}
