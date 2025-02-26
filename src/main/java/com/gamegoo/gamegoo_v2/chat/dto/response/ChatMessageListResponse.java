package com.gamegoo.gamegoo_v2.chat.dto.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class ChatMessageListResponse {

    List<ChatMessageResponse> chatMessageList;
    int listSize;
    Boolean hasNext;
    Long nextCursor;

}
