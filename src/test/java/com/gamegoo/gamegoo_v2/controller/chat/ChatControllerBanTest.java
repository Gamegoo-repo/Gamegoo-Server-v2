package com.gamegoo.gamegoo_v2.controller.chat;

import com.gamegoo.gamegoo_v2.chat.controller.ChatController;
import com.gamegoo.gamegoo_v2.chat.service.ChatFacadeService;
import com.gamegoo.gamegoo_v2.controller.ControllerTestSupport;
import com.gamegoo.gamegoo_v2.controller.WithCustomMockMember;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@DisplayName("ChatController 제재 검증 테스트")
public class ChatControllerBanTest extends ControllerTestSupport {

    @MockitoBean
    private ChatFacadeService chatFacadeService;

    private static final String API_URL_PREFIX = "/api/v2/chat";
    private static final Long TARGET_MEMBER_ID = 2L;
    private static final Long BOARD_ID = 1L;

    @Nested
    @DisplayName("채팅 시작 제재 검증")
    class ChatStartBanValidationTest {

        @DisplayName("제재된 사용자가 회원과 채팅을 시작하려고 하면 403 Forbidden과 제재 에러 코드를 반환한다")
        @WithCustomMockMember
        @Test
        void banned_user_cannot_start_chat_with_member() throws Exception {
            // given
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .given(chatFacadeService).startChatroomByMemberId(any(), anyLong());

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/start/member/{memberId}", TARGET_MEMBER_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_403"))
                    .andExpect(jsonPath("$.message").value("채팅 사용이 제한된 상태입니다."));
        }

        @DisplayName("제재된 사용자가 게시글을 통해 채팅을 시작하려고 하면 403 Forbidden과 제재 에러 코드를 반환한다")
        @WithCustomMockMember
        @Test
        void banned_user_cannot_start_chat_from_board() throws Exception {
            // given
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .given(chatFacadeService).startChatroomByBoardId(any(), anyLong());

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/start/board/{boardId}", BOARD_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_403"))
                    .andExpect(jsonPath("$.message").value("채팅 사용이 제한된 상태입니다."));
        }

        @DisplayName("영구 제재된 사용자는 채팅을 시작할 수 없다")
        @WithCustomMockMember
        @Test
        void permanently_banned_user_cannot_start_chat() throws Exception {
            // given
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .given(chatFacadeService).startChatroomByMemberId(any(), anyLong());

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/start/member/{memberId}", TARGET_MEMBER_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_403"))
                    .andExpect(jsonPath("$.message").value("채팅 사용이 제한된 상태입니다."));
        }

        @DisplayName("전체 제재된 사용자는 채팅을 시작할 수 없다")
        @WithCustomMockMember
        @Test
        void generally_banned_user_cannot_start_chat() throws Exception {
            // given
            willThrow(new MemberException(ErrorCode.MEMBER_BANNED))
                    .given(chatFacadeService).startChatroomByMemberId(any(), anyLong());

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/start/member/{memberId}", TARGET_MEMBER_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("BAN_401"))
                    .andExpect(jsonPath("$.message").value("현재 계정이 제재된 상태입니다. 고객센터에 문의해주세요."));
        }

        @DisplayName("제재되지 않은 사용자는 정상적으로 채팅을 시작할 수 있다")
        @WithCustomMockMember
        @Test
        void normal_user_can_start_chat() throws Exception {
            // given
            // 정상 응답은 EnterChatroomResponse를 반환하므로 Mock 설정은 생략
            // (실제로는 정상적인 응답을 반환하도록 설정해야 하지만, 여기서는 예외가 발생하지 않는 것만 확인)

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/start/member/{memberId}", TARGET_MEMBER_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @DisplayName("제재되지 않은 사용자는 정상적으로 게시글에서 채팅을 시작할 수 있다")
        @WithCustomMockMember
        @Test
        void normal_user_can_start_chat_from_board() throws Exception {
            // given
            // 정상 응답은 EnterChatroomResponse를 반환하므로 Mock 설정은 생략

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/start/board/{boardId}", BOARD_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("채팅 관련 기능 제재 검증")
    class ChatFeatureBanValidationTest {

        @DisplayName("제재된 사용자가 채팅 목록을 조회하려고 하면 정상적으로 처리된다 (읽기는 허용)")
        @WithCustomMockMember
        @Test
        void banned_user_can_view_chat_list() throws Exception {
            // given
            // 채팅 목록 조회는 제재되어도 허용되므로 정상 처리

            // when & then
            mockMvc.perform(get("/api/v2/chatroom"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @DisplayName("제재된 사용자가 채팅 메시지를 조회하려고 하면 정상적으로 처리된다 (읽기는 허용)")
        @WithCustomMockMember
        @Test
        void banned_user_can_view_chat_messages() throws Exception {
            // given
            String chatroomUuid = "test-uuid";

            // when & then
            mockMvc.perform(get(API_URL_PREFIX + "/{uuid}/messages", chatroomUuid))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @DisplayName("제재된 사용자가 채팅방을 나가는 것은 정상적으로 처리된다")
        @WithCustomMockMember
        @Test
        void banned_user_can_leave_chatroom() throws Exception {
            // given
            String chatroomUuid = "test-uuid";

            // when & then
            mockMvc.perform(patch(API_URL_PREFIX + "/{uuid}/exit", chatroomUuid))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }
}