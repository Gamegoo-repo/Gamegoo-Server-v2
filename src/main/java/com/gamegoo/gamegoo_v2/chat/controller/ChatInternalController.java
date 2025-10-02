package com.gamegoo.gamegoo_v2.chat.controller;

import com.gamegoo.gamegoo_v2.chat.dto.request.ChatCreateRequest;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatCreateResponse;
import com.gamegoo.gamegoo_v2.chat.service.ChatFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.config.swagger.ApiErrorCodes;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Chat Internal", description = "Chat 관련 internal API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/internal")
public class ChatInternalController {

    private final ChatFacadeService chatFacadeService;

    @Operation(summary = "채팅방 uuid 조회 API", description = "회원이 속한 채팅방의 uuid를 조회하는 API 입니다.")
    @GetMapping("/{memberId}/chatroom/uuid")
    @ApiErrorCodes({ErrorCode.MEMBER_NOT_FOUND})
    public ApiResponse<List<String>> getChatroomUuid(@PathVariable(name = "memberId") Long memberId) {
        return ApiResponse.ok(chatFacadeService.getChatroomUuids(memberId));
    }

    @Operation(summary = "채팅 메시지 등록 API", description = "새로운 채팅 메시지를 등록하는 API 입니다.")
    @PostMapping("/{memberId}/chat/{chatroomUuid}")
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.MEMBER_BANNED_FROM_CHATTING,
            ErrorCode.CHATROOM_NOT_FOUND,
            ErrorCode.CHATROOM_ACCESS_DENIED,
            ErrorCode.CHAT_ADD_FAILED_TARGET_DEACTIVATED,
            ErrorCode.CHAT_ADD_FAILED_TARGET_IS_BLOCKED,
            ErrorCode.CHAT_ADD_FAILED_BLOCKED_BY_TARGET,
            ErrorCode.ADD_BOARD_SYSTEM_CHAT_FAILED,
            ErrorCode.SYSTEM_MEMBER_NOT_FOUND,
            ErrorCode.SYSTEM_MESSAGE_TYPE_NOT_FOUND
    })
    public ApiResponse<ChatCreateResponse> addChat(@PathVariable(name = "memberId") Long memberId,
                                                   @PathVariable(name = "chatroomUuid") String chatroomUuid,
                                                   @RequestBody @Valid ChatCreateRequest request) {
        return ApiResponse.ok(chatFacadeService.createChat(request, memberId, chatroomUuid));
    }

}
