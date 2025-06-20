package com.gamegoo.gamegoo_v2.chat.domain;

import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum SystemMessageType {

    INITIATE_CHATROOM_BY_BOARD_MESSAGE(1, "상대방이 게시한 글을 보고 말을 걸었어요. 대화를 시작해보세요~"),
    CHAT_STARTED_BY_BOARD_MESSAGE(2, "상대방이 게시한 글을 보고 말을 걸었어요."),
    INCOMING_CHAT_BY_BOARD_MESSAGE(3, "내가 게시한 글을 보고 말을 걸어왔어요."),
    MATCH_SUCCESS_MESSAGE(4, "상대방과 매칭이 이루어졌어요!"),
    MANNER_MESSAGE(5, "매칭은 어떠셨나요? 상대방의 매너를 평가해주세요!");

    private final int code;
    private final String message;

    SystemMessageType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // code로 GameMode 객체 조회하기 위한 map
    private static final Map<Integer, SystemMessageType> SYSTEM_MESSAGE_TYPE_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(SystemMessageType::getCode, systemMessageType -> systemMessageType));

    public static SystemMessageType of(int id) {
        SystemMessageType systemMessageType = SYSTEM_MESSAGE_TYPE_MAP.get(id);
        if (systemMessageType == null) {
            throw new ChatException(ErrorCode.SYSTEM_MESSAGE_TYPE_NOT_FOUND);
        }
        return systemMessageType;
    }
}
