package com.gamegoo.gamegoo_v2.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatroomListResponse {

    List<ChatroomResponse> chatroomResponseList;
    int listSize;

}
