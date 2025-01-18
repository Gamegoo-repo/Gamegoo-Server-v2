package com.gamegoo.gamegoo_v2.controller.internal;

import com.gamegoo.gamegoo_v2.chat.controller.ChatInternalController;
import com.gamegoo.gamegoo_v2.chat.dto.request.ChatCreateRequest;
import com.gamegoo.gamegoo_v2.chat.dto.request.SystemFlagRequest;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatCreateResponse;
import com.gamegoo.gamegoo_v2.chat.service.ChatFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.social.friend.controller.FriendInternalController;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendFacadeService;
import com.gamegoo.gamegoo_v2.utils.DateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({FriendInternalController.class, ChatInternalController.class})
@AutoConfigureMockMvc(addFilters = false) // internal은 security filter 전부 무시하도록 설정
public class InternalControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendFacadeService friendFacadeService;

    @MockitoBean
    private ChatFacadeService chatFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/internal";
    private static final Long MEMBER_ID = 1L;
    private static final String TARGET_CHATROOM_UUID = "test-uuid";
    private static final Long TARGET_BOARD_ID = 1L;

    @DisplayName("모든 친구 id 조회 성공")
    @Test
    void getFriendIdListSucceeds() throws Exception {
        // given
        List<Long> response = new ArrayList<>();

        given(friendFacadeService.getFriendIdList(any(Long.class))).willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/{memberId}/friend/ids", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @DisplayName("모든 채팅방 uuid 조회 성공")
    @Test
    void getChatroomUuidSucceeds() throws Exception {
        // given
        List<String> response = new ArrayList<>();

        given(chatFacadeService.getChatroomUuids(any(Long.class))).willReturn(response);

        // when // then
        mockMvc.perform(get(API_URL_PREFIX + "/{memberId}/chatroom/uuid", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Nested
    @DisplayName("채팅 메시지 등록")
    class AddChatTest {

        @DisplayName("실패: message가 empty인 경우 에러 응답을 반환한다.")
        @Test
        void addChatFailedWhenMessageIsEmpty() throws Exception {
            // given
            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("")
                    .system(null)
                    .build();

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}/chat/{chatroomUuid}", MEMBER_ID, TARGET_CHATROOM_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("message는 필수 값 입니다."));
        }

        @DisplayName("실패: system flag가 null인 경우 에러 응답을 반환한다.")
        @Test
        void addChatFailedWhenFlagIsNull() throws Exception {
            // given
            SystemFlagRequest systemFlagRequest = SystemFlagRequest.builder()
                    .flag(null)
                    .boardId(TARGET_BOARD_ID)
                    .build();

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(systemFlagRequest)
                    .build();

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}/chat/{chatroomUuid}", MEMBER_ID, TARGET_CHATROOM_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("flag는 필수 값 입니다."));
        }

        @DisplayName("살패: system flag가 1보다 작은 경우 에러 응답을 반환한다.")
        @Test
        void addChatFailedWhenFlagIsLessThan() throws Exception {
            // given
            SystemFlagRequest systemFlagRequest = SystemFlagRequest.builder()
                    .flag(0)
                    .boardId(TARGET_BOARD_ID)
                    .build();

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(systemFlagRequest)
                    .build();

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}/chat/{chatroomUuid}", MEMBER_ID, TARGET_CHATROOM_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("flag는 1 이상의 값이어야 합니다."));
        }

        @DisplayName("살패: system flag가 2보다 큰 경우 에러 응답을 반환한다.")
        @Test
        void addChatFailedWhenFlagIsGreaterThan() throws Exception {
            // given
            SystemFlagRequest systemFlagRequest = SystemFlagRequest.builder()
                    .flag(3)
                    .boardId(TARGET_BOARD_ID)
                    .build();

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(systemFlagRequest)
                    .build();

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}/chat/{chatroomUuid}", MEMBER_ID, TARGET_CHATROOM_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("flag는 2 이하의 값이어야 합니다."));
        }

        @DisplayName("실패: boardId가 null인 경우 에러 응답을 반환한다.")
        @Test
        void addChatFailedWhenBoardIdIsNull() throws Exception {
            // given
            SystemFlagRequest systemFlagRequest = SystemFlagRequest.builder()
                    .flag(1)
                    .boardId(null)
                    .build();

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(systemFlagRequest)
                    .build();

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}/chat/{chatroomUuid}", MEMBER_ID, TARGET_CHATROOM_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALID_ERROR"))
                    .andExpect(jsonPath("$.message").value("boardId는 필수 값 입니다."));
        }

        @DisplayName("성공: 생성된 메시지 정보를 반환한다.")
        @Test
        void addChatSucceeds() throws Exception {
            // given
            SystemFlagRequest systemFlagRequest = SystemFlagRequest.builder()
                    .flag(1)
                    .boardId(TARGET_BOARD_ID)
                    .build();

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(systemFlagRequest)
                    .build();

            ChatCreateResponse response = ChatCreateResponse.builder()
                    .senderId(MEMBER_ID)
                    .senderName("member")
                    .senderProfileImg(1)
                    .message("message")
                    .createdAt(DateTimeUtil.toKSTString(LocalDateTime.now()))
                    .timestamp(123456789012L)
                    .build();

            given(chatFacadeService
                    .createChat(any(ChatCreateRequest.class), any(Long.class), eq(TARGET_CHATROOM_UUID)))
                    .willReturn(response);

            // when // then
            mockMvc.perform(post(API_URL_PREFIX + "/{memberId}/chat/{chatroomUuid}", MEMBER_ID, TARGET_CHATROOM_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.data.senderId").value(MEMBER_ID))
                    .andExpect(jsonPath("$.data.senderName").value("member"))
                    .andExpect(jsonPath("$.data.senderProfileImg").value(1))
                    .andExpect(jsonPath("$.data.message").value("message"))
                    .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.timestamp").value(123456789012L));
        }

    }

}
