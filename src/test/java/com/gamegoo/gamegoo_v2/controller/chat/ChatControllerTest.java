package com.gamegoo.gamegoo_v2.controller.chat;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.chat.controller.ChatController;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.EnterChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.EnterChatroomResponse.SystemFlagResponse;
import com.gamegoo.gamegoo_v2.chat.service.ChatFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ChatFacadeService chatFacadeService;

    private static final String API_URL_PREFIX = "/api/v2";
    private static final Long MEMBER_ID = 1L;
    private static final Long TARGET_MEMBER_ID = 2L;
    private static final Long TARGET_BOARD_ID = 1L;
    private static final String TARGET_CHATROOM_UUID = "test-uuid";
    private static final String TARGET_GAMENAME = "targetMember";

    @DisplayName("특정 회원과 채팅방 시작 성공: 채팅방 및 상대 회원 정보, 대화 내역이 반환된다.")
    @Test
    void startChatroomByMemberIdSucceeds() throws Exception {
        // given
        ChatMessageListResponse chatMessageListResponse = ChatMessageListResponse.builder()
                .chatMessageList(new ArrayList<>())
                .listSize(0)
                .hasNext(false)
                .nextCursor(null)
                .build();

        EnterChatroomResponse response = EnterChatroomResponse.builder()
                .memberId(TARGET_MEMBER_ID)
                .gameName(TARGET_GAMENAME)
                .memberProfileImg(1)
                .blind(false)
                .blocked(false)
                .friend(false)
                .friendRequestMemberId(null)
                .uuid(TARGET_CHATROOM_UUID)
                .system(null)
                .chatMessageListResponse(chatMessageListResponse)
                .build();

        given(chatFacadeService.startChatroomByMemberId(any(Member.class), eq(TARGET_MEMBER_ID)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/chat/start/member/{memberId}", TARGET_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.chatMessageListResponse").isNotEmpty())
                .andExpect(jsonPath("$.data.memberId").value(TARGET_MEMBER_ID))
                .andExpect(jsonPath("$.data.gameName").value(TARGET_GAMENAME))
                .andExpect(jsonPath("$.data.memberProfileImg").value(1))
                .andExpect(jsonPath("$.data.blind").value(false))
                .andExpect(jsonPath("$.data.blocked").value(false))
                .andExpect(jsonPath("$.data.friend").value(false))
                .andExpect(jsonPath("$.data.friendRequestMemberId").isEmpty())
                .andExpect(jsonPath("$.data.uuid").value(TARGET_CHATROOM_UUID))
                .andExpect(jsonPath("$.data.system").isEmpty());
    }

    @DisplayName("특정 글을 통한 채팅방 시작 성공: 채팅방 및 상대 회원 정보, 시스템 메시지 정보, 대화 내역이 반환된다.")
    @Test
    void startChatroomByBoardIdSucceeds() throws Exception {
        // given
        ChatMessageListResponse chatMessageListResponse = ChatMessageListResponse.builder()
                .chatMessageList(new ArrayList<>())
                .listSize(0)
                .hasNext(false)
                .nextCursor(null)
                .build();

        SystemFlagResponse systemFlagResponse = SystemFlagResponse.builder()
                .flag(1)
                .boardId(TARGET_BOARD_ID)
                .build();

        EnterChatroomResponse response = EnterChatroomResponse.builder()
                .memberId(TARGET_MEMBER_ID)
                .gameName(TARGET_GAMENAME)
                .memberProfileImg(1)
                .blind(false)
                .blocked(false)
                .friend(false)
                .friendRequestMemberId(null)
                .uuid(TARGET_CHATROOM_UUID)
                .system(systemFlagResponse)
                .chatMessageListResponse(chatMessageListResponse)
                .build();

        given(chatFacadeService.startChatroomByBoardId(any(Member.class), eq(TARGET_BOARD_ID)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/chat/start/board/{boardId}", TARGET_BOARD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.chatMessageListResponse").isNotEmpty())
                .andExpect(jsonPath("$.data.memberId").value(TARGET_MEMBER_ID))
                .andExpect(jsonPath("$.data.gameName").value(TARGET_GAMENAME))
                .andExpect(jsonPath("$.data.memberProfileImg").value(1))
                .andExpect(jsonPath("$.data.blind").value(false))
                .andExpect(jsonPath("$.data.blocked").value(false))
                .andExpect(jsonPath("$.data.friend").value(false))
                .andExpect(jsonPath("$.data.friendRequestMemberId").isEmpty())
                .andExpect(jsonPath("$.data.uuid").value(TARGET_CHATROOM_UUID))
                .andExpect(jsonPath("$.data.system").isNotEmpty())
                .andExpect(jsonPath("$.data.system.flag").value(1))
                .andExpect(jsonPath("$.data.system.boardId").value(TARGET_BOARD_ID));
    }

    @DisplayName("특정 채팅방 입장 성공: 채팅방 및 상대 회원 정보, 대화 내역이 반환된다.")
    @Test
    void enterChatroomSucceeds() throws Exception {
        // given
        ChatMessageListResponse chatMessageListResponse = ChatMessageListResponse.builder()
                .chatMessageList(new ArrayList<>())
                .listSize(0)
                .hasNext(false)
                .nextCursor(null)
                .build();

        EnterChatroomResponse response = EnterChatroomResponse.builder()
                .memberId(TARGET_MEMBER_ID)
                .gameName(TARGET_GAMENAME)
                .memberProfileImg(1)
                .blind(false)
                .blocked(false)
                .friend(false)
                .friendRequestMemberId(null)
                .uuid(TARGET_CHATROOM_UUID)
                .system(null)
                .chatMessageListResponse(chatMessageListResponse)
                .build();

        given(chatFacadeService.enterChatroomByUuid(any(Member.class), eq(TARGET_CHATROOM_UUID)))
                .willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/chat/{chatroomUuid}/enter", TARGET_CHATROOM_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.chatMessageListResponse").isNotEmpty())
                .andExpect(jsonPath("$.data.memberId").value(TARGET_MEMBER_ID))
                .andExpect(jsonPath("$.data.gameName").value(TARGET_GAMENAME))
                .andExpect(jsonPath("$.data.memberProfileImg").value(1))
                .andExpect(jsonPath("$.data.blind").value(false))
                .andExpect(jsonPath("$.data.blocked").value(false))
                .andExpect(jsonPath("$.data.friend").value(false))
                .andExpect(jsonPath("$.data.friendRequestMemberId").isEmpty())
                .andExpect(jsonPath("$.data.uuid").value(TARGET_CHATROOM_UUID))
                .andExpect(jsonPath("$.data.system").isEmpty());

    }

    @Nested
    @DisplayName("채팅 내역 조회")
    class GetChatMessagesTest {

        @DisplayName("성공: cursor가 없는 경우")
        @Test
        void getChatMessagesSucceedsWhenNoCursor() throws Exception {
            // given
            List<ChatMessageResponse> chatMessageResponseList = new ArrayList<>();
            ChatMessageListResponse response = ChatMessageListResponse.builder()
                    .chatMessageList(chatMessageResponseList)
                    .listSize(0)
                    .nextCursor(null)
                    .hasNext(false)
                    .build();

            given(chatFacadeService.getChatMessagesByCursor(any(Member.class), any(String.class), any()))
                    .willReturn(response);

            // when // then
            mockMvc.perform(get(API_URL_PREFIX + "/chat/{chatroomUuid}/messages", TARGET_CHATROOM_UUID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.chatMessageList").isArray())
                    .andExpect(jsonPath("$.data.listSize").isNumber())
                    .andExpect(jsonPath("$.data.hasNext").isBoolean());
        }

        @DisplayName("성공: cursor가 있는 경우")
        @Test
        void getChatMessagesSucceedsWithCursor() throws Exception {
            // given
            List<ChatMessageResponse> chatMessageResponseList = new ArrayList<>();
            ChatMessageListResponse response = ChatMessageListResponse.builder()
                    .chatMessageList(chatMessageResponseList)
                    .listSize(0)
                    .nextCursor(null)
                    .hasNext(false)
                    .build();

            given(chatFacadeService.getChatMessagesByCursor(any(Member.class), any(String.class), any()))
                    .willReturn(response);

            // when // then
            mockMvc.perform(get(API_URL_PREFIX + "/chat/{chatroomUuid}/messages", TARGET_CHATROOM_UUID)
                            .param("cursor", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.chatMessageList").isArray())
                    .andExpect(jsonPath("$.data.listSize").isNumber())
                    .andExpect(jsonPath("$.data.hasNext").isBoolean());
        }

        @DisplayName("실패: cursor가 음수인 경우")
        @Test
        void getChatMessagesFailedWhenCursorIsNegative() throws Exception {
            // when // then
            mockMvc.perform(get(API_URL_PREFIX + "/chat/{chatroomUuid}/messages", TARGET_CHATROOM_UUID)
                            .param("cursor", "-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("커서는 1 이상의 값이어야 합니다."));
        }

    }

    @DisplayName("안읽은 채팅방 uuid 조회 성공")
    @Test
    void GetUnreadChatroomUuidSucceeds() throws Exception {
        // given
        List<String> response = List.of();

        given(chatFacadeService.getUnreadChatroomUuids(any(Member.class))).willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/chat/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Nested
    @DisplayName("채팅방 읽음 처리")
    class ReadChatMessageTest {

        @DisplayName("성공: timestamp가 없는 경우")
        @Test
        void readChatMessageSucceedsWhenTimestampIsNull() throws Exception {
            // given
            String response = "채팅방 읽음 처리 성공";

            given(chatFacadeService.readChatMessage(any(Member.class), any(String.class), any()))
                    .willReturn(response);

            // when // then
            mockMvc.perform(patch(API_URL_PREFIX + "/chat/{chatroomUuid}/read", TARGET_CHATROOM_UUID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").value("채팅방 읽음 처리 성공"));
        }

        @DisplayName("성공: timestamp가 있는 경우")
        @Test
        void readChatMessageSucceeds() throws Exception {
            // given
            String response = "채팅방 읽음 처리 성공";

            given(chatFacadeService.readChatMessage(any(Member.class), any(String.class), any()))
                    .willReturn(response);

            // when // then
            mockMvc.perform(patch(API_URL_PREFIX + "/chat/{chatroomUuid}/read", TARGET_CHATROOM_UUID)
                            .param("timestamp", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data").value("채팅방 읽음 처리 성공"));
        }

    }

    @DisplayName("채팅방 나가기 성공")
    @Test
    void ExitChatroomSucceeds() throws Exception {
        // given
        String response = "채팅방 나가기 성공";

        given(chatFacadeService.exitChatroom(any(Member.class), any(String.class))).willReturn(response);

        // when // then
        mockMvc.perform(patch(API_URL_PREFIX + "/chat/{chatroomUuid}/exit", TARGET_CHATROOM_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").value("채팅방 나가기 성공"));
    }

    @DisplayName("채팅방 목록 조회")
    @Test
    void getChatroomSucceeds() throws Exception {
        // given
        List<ChatroomResponse> chatroomResponseList = new ArrayList<>();
        ChatroomListResponse response = ChatroomListResponse.builder()
                .chatroomResponseList(chatroomResponseList)
                .listSize(0)
                .build();

        given(chatFacadeService.getChatrooms(any(Member.class))).willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/chatroom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.chatroomResponseList").isArray())
                .andExpect(jsonPath("$.data.listSize").isNumber());
    }

}
