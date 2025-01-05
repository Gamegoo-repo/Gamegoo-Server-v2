package com.gamegoo.gamegoo_v2.chat.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.EnterChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.service.ChatFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidCursor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Chat", description = "Chat 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Validated
public class ChatController {

    private final ChatFacadeService chatFacadeService;

    @Operation(summary = "특정 회원과 채팅방 시작 API",
            description = "특정 대상 회원과의 채팅방을 시작하는 API 입니다.\n\n" +
                    "대상 회원과의 채팅방이 이미 존재하는 경우, 채팅방 uuid, 상대 회원 정보와 채팅 메시지 내역 등을 반환합니다.\n\n" +
                    "대상 회원과의 채팅방이 존재하지 않는 경우, 채팅방을 새로 생성해 해당 채팅방의 uuid, 상대 회원 정보 등을 반환합니다.")
    @Parameter(name = "memberId", description = "채팅방을 시작할 대상 회원의 id 입니다.")
    @GetMapping("/chat/start/member/{memberId}")
    public ApiResponse<EnterChatroomResponse> startChatroomByMemberId(
            @PathVariable(name = "memberId") Long targetMemberId,
            @AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.startChatroomByMemberId(member, targetMemberId));
    }

    @Operation(summary = "특정 글을 통한 채팅방 시작 API",
            description = "특정 글에서 말 걸어보기 버튼을 통해 채팅방을 시작하는 API 입니다.\n\n" +
                    "대상 회원과의 채팅방이 이미 존재하는 경우, 채팅방 uuid, 상대 회원 정보와 채팅 메시지 내역 등을 리턴합니다.\n\n" +
                    "대상 회원과의 채팅방이 존재하지 않는 경우, 채팅방을 새로 생성해 해당 채팅방의 uuid, 상대 회원 정보 등을 리턴합니다.")
    @Parameter(name = "boardId", description = "말 걸어보기 버튼을 누른 게시글의 id 입니다.")
    @GetMapping("/chat/start/board/{boardId}")
    public ApiResponse<EnterChatroomResponse> startChatroomByBoardId(@PathVariable(name = "boardId") Long boardId,
                                                                     @AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.startChatroomByBoardId(member, boardId));
    }

    @Operation(summary = "채팅방 입장 API",
            description = "특정 채팅방에 입장하는 API 입니다. 상대 회원 정보와 채팅 메시지 내역 등을 리턴합니다.")
    @GetMapping("/chat/{chatroomUuid}/enter")
    public ApiResponse<EnterChatroomResponse> enterChatroom(@PathVariable(name = "chatroomUuid") String chatroomUuid,
                                                            @AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.enterChatroomByUuid(member, chatroomUuid));
    }

    @Operation(summary = "채팅 내역 조회 API",
            description = "특정 채팅방의 메시지 내역을 조회하는 API 입니다.\n\n" +
                    "cursor 파라미터를 보내면, 해당 timestamp 이전에 전송된 메시지 최대 20개를 조회합니다.\n\n" +
                    "cursor 파라미터를 보내지 않으면, 해당 채팅방의 가장 최근 메시지 내역을 조회합니다.")
    @GetMapping("/chat/{chatroomUuid}/messages")
    @Parameter(name = "cursor", description = "페이징을 위한 커서, 13자리 timestamp integer를 보내주세요. (UTC 기준)")
    public ApiResponse<ChatMessageListResponse> getChatMessages(
            @PathVariable(name = "chatroomUuid") String chatroomUuid,
            @ValidCursor @RequestParam(name = "cursor", required = false) Long cursor,
            @AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.getChatMessagesByCursor(member, chatroomUuid, cursor));
    }

    @Operation(summary = "안읽은 채팅방 uuid 목록 조회 API", description = "안읽은 메시지가 속한 채팅방의 uuid 목록을 조회하는 API 입니다.")
    @GetMapping("/chat/unread")
    public ApiResponse<List<String>> getUnreadChatroomUuid(@AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.getUnreadChatroomUuids(member));
    }

    @Operation(summary = "채팅 메시지 읽음 처리 API", description = "특정 채팅방의 메시지를 읽음 처리하는 API 입니다.")
    @PatchMapping("/chat/{chatroomUuid}/read")
    @Parameter(name = "timestamp", description = "특정 메시지를 읽음 처리하는 경우, 그 메시지의 timestamp를 함께 보내주세요.")
    public ApiResponse<String> readChatMessage(
            @PathVariable(name = "chatroomUuid") String chatroomUuid,
            @RequestParam(name = "timestamp", required = false) Long timestamp,
            @AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.readChatMessage(member, chatroomUuid, timestamp));
    }

    @Operation(summary = "채팅방 나가기 API", description = "채팅방 나가기 API 입니다.")
    @PatchMapping("/chat/{chatroomUuid}/exit")
    public ApiResponse<Object> exitChatroom(@PathVariable(name = "chatroomUuid") String chatroomUuid,
                                            @AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.exitChatroom(member, chatroomUuid));
    }

    @Operation(summary = "채팅방 목록 조회 API", description = "회원이 속한 채팅방 목록을 조회하는 API 입니다.")
    @GetMapping("/chatroom")
    public ApiResponse<ChatroomListResponse> getChatroom(@AuthMember Member member) {
        return ApiResponse.ok(chatFacadeService.getChatrooms(member));
    }

}
