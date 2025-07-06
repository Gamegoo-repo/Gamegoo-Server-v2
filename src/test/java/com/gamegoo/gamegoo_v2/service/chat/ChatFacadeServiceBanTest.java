package com.gamegoo.gamegoo_v2.service.chat;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.dto.ChatResponseFactory;
import com.gamegoo.gamegoo_v2.chat.dto.request.ChatCreateRequest;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.chat.service.ChatFacadeService;
import com.gamegoo.gamegoo_v2.chat.service.ChatQueryService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.core.common.validator.BanValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.BlockValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.ChatValidator;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatFacadeService 제재 검증 테스트")
class ChatFacadeServiceBanTest {

    @Mock
    private MemberService memberService;

    @Mock
    private BoardService boardService;

    @Mock
    private ChatCommandService chatCommandService;

    @Mock
    private ChatQueryService chatQueryService;

    @Mock
    private MemberValidator memberValidator;

    @Mock
    private BlockValidator blockValidator;

    @Mock
    private BanValidator banValidator;

    @Mock
    private ChatValidator chatValidator;

    @Mock
    private ChatResponseFactory chatResponseFactory;

    @InjectMocks
    private ChatFacadeService chatFacadeService;

    private Member bannedMember;
    private Member normalMember;
    private Chatroom chatroom;
    private Board board;

    @BeforeEach
    void setUp() {
        bannedMember = createMember(BanType.BAN_1D, LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(bannedMember, "id", 1L);

        normalMember = createMember(BanType.NONE, null);
        ReflectionTestUtils.setField(normalMember, "id", 2L);

        chatroom = Chatroom.builder()
                .uuid("test-uuid")
                .build();
        ReflectionTestUtils.setField(chatroom, "id", 1L);

        board = Board.builder()
                .member(normalMember)
                .gameMode(com.gamegoo.gamegoo_v2.matching.domain.GameMode.SOLO)
                .mainP(com.gamegoo.gamegoo_v2.account.member.domain.Position.TOP)
                .subP(com.gamegoo.gamegoo_v2.account.member.domain.Position.JUNGLE)
                .wantP(java.util.List.of(com.gamegoo.gamegoo_v2.account.member.domain.Position.ANY))
                .mike(com.gamegoo.gamegoo_v2.account.member.domain.Mike.AVAILABLE)
                .content("test content")
                .boardProfileImage(1)
                .build();
        ReflectionTestUtils.setField(board, "id", 1L);
    }

    @Nested
    @DisplayName("채팅 전송 제재 검증")
    class ChatSendingBanValidationTest {

        @Test
        @DisplayName("제재된 사용자가 채팅을 전송하려고 하면 BanValidator에서 예외가 발생한다")
        void banned_user_cannot_send_chat() {
            // given
            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("test message")
                    .build();

            when(memberService.findMemberById(anyLong())).thenReturn(bannedMember);
            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .when(banValidator).throwIfBannedFromChatting(bannedMember);

            // when & then
            assertThatThrownBy(() -> chatFacadeService.createChat(request, 1L, "test-uuid"))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getCode());

            // 제재 검증에서 실패하면 이후 로직은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromChatting(bannedMember);
            verify(chatQueryService, never()).getChatroomByUuid(anyString());
            verify(chatCommandService, never()).createMemberChat(any(), any(), anyString());
        }

        @Test
        @DisplayName("제재되지 않은 사용자는 채팅을 정상적으로 전송할 수 있다")
        void normal_user_can_send_chat() {
            // given
            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("test message")
                    .system(null)  // Explicitly set to null for regular chat path
                    .build();

            Chat chat = Chat.builder()
                    .contents("test message")
                    .timestamp(System.currentTimeMillis())
                    .chatroom(chatroom)
                    .fromMember(normalMember)
                    .build();
            ReflectionTestUtils.setField(chat, "id", 1L);
            ReflectionTestUtils.setField(chat, "createdAt", java.time.LocalDateTime.now());

            when(memberService.findMemberById(anyLong())).thenReturn(normalMember);
            doNothing().when(banValidator).throwIfBannedFromChatting(normalMember);
            when(chatQueryService.getChatroomByUuid(anyString())).thenReturn(chatroom);
            when(chatValidator.validateMemberChatroom(anyLong(), anyLong())).thenReturn(null);
            when(chatQueryService.getChatroomTargetMember(any(), any())).thenReturn(normalMember);
            doNothing().when(memberValidator).throwIfBlind(any(), any(), any());
            doNothing().when(blockValidator).throwIfBlocked(any(), any(), any(), any());
            when(chatCommandService.createMemberChat(any(), any(), anyString())).thenReturn(chat);
            doNothing().when(chatCommandService).updateMemberChatroomDatesByAddChat(any(), any(), any());
            doNothing().when(chatCommandService).updateLastChat(any(), any());

            // when
            chatFacadeService.createChat(request, 1L, "test-uuid");

            // then
            verify(banValidator).throwIfBannedFromChatting(normalMember);
            // 제재 검증 통과 후 정상 로직이 실행되어야 함
            verify(chatQueryService).getChatroomByUuid(anyString());
            verify(chatCommandService).createMemberChat(any(), any(), anyString());
        }

        @Test
        @DisplayName("영구 제재된 사용자는 채팅을 전송할 수 없다")
        void permanently_banned_user_cannot_send_chat() {
            // given
            Member permanentBannedMember = createMember(BanType.PERMANENT, null);
            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("test message")
                    .build();

            when(memberService.findMemberById(anyLong())).thenReturn(permanentBannedMember);
            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .when(banValidator).throwIfBannedFromChatting(permanentBannedMember);

            // when & then
            assertThatThrownBy(() -> chatFacadeService.createChat(request, 1L, "test-uuid"))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getCode());

            verify(banValidator).throwIfBannedFromChatting(permanentBannedMember);
        }

        @Test
        @DisplayName("경고 제재 사용자는 채팅을 정상적으로 전송할 수 있다")
        void warning_banned_user_can_send_chat() {
            // given
            Member warningMember = createMember(BanType.WARNING, null);
            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("test message")
                    .system(null)  // Explicitly set to null for regular chat path
                    .build();

            Chat chat = Chat.builder()
                    .contents("test message")
                    .timestamp(System.currentTimeMillis())
                    .chatroom(chatroom)
                    .fromMember(warningMember)
                    .build();
            ReflectionTestUtils.setField(chat, "id", 1L);
            ReflectionTestUtils.setField(chat, "createdAt", java.time.LocalDateTime.now());

            when(memberService.findMemberById(anyLong())).thenReturn(warningMember);
            doNothing().when(banValidator).throwIfBannedFromChatting(warningMember);
            when(chatQueryService.getChatroomByUuid(anyString())).thenReturn(chatroom);
            when(chatValidator.validateMemberChatroom(anyLong(), anyLong())).thenReturn(null);
            when(chatQueryService.getChatroomTargetMember(any(), any())).thenReturn(normalMember);
            doNothing().when(memberValidator).throwIfBlind(any(), any(), any());
            doNothing().when(blockValidator).throwIfBlocked(any(), any(), any(), any());
            when(chatCommandService.createMemberChat(any(), any(), anyString())).thenReturn(chat);
            doNothing().when(chatCommandService).updateMemberChatroomDatesByAddChat(any(), any(), any());
            doNothing().when(chatCommandService).updateLastChat(any(), any());

            // when
            chatFacadeService.createChat(request, 1L, "test-uuid");

            // then
            verify(banValidator).throwIfBannedFromChatting(warningMember);
            // 경고는 실제 제재가 아니므로 정상 로직이 실행되어야 함
            verify(chatQueryService).getChatroomByUuid(anyString());
            verify(chatCommandService).createMemberChat(any(), any(), anyString());
        }
    }

    @Nested
    @DisplayName("게시판 말걸어보기 제재 검증")
    class BoardChatStartBanValidationTest {

        @Test
        @DisplayName("제재된 사용자가 게시판 말걸어보기를 시도하면 BanValidator에서 예외가 발생한다")
        void banned_user_cannot_start_chat_from_board() {
            // given
            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .when(banValidator).throwIfBannedFromChatting(bannedMember);

            // when & then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(bannedMember, 1L))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.MEMBER_BANNED_FROM_CHATTING.getCode());

            // 제재 검증에서 실패하면 이후 로직은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromChatting(bannedMember);
            verify(memberService, never()).findMemberById(anyLong());
            verify(memberValidator, never()).throwIfEqual(any(), any());
        }

        @Test
        @DisplayName("제재되지 않은 사용자는 게시판 말걸어보기를 정상적으로 할 수 있다")
        void normal_user_can_start_chat_from_board() {
            // given
            com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom memberChatroom = com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom.builder()
                    .member(normalMember)
                    .chatroom(chatroom)
                    .lastJoinDate(java.time.LocalDateTime.now())
                    .build();
            ReflectionTestUtils.setField(memberChatroom, "id", 1L);

            doNothing().when(banValidator).throwIfBannedFromChatting(normalMember);
            when(boardService.findBoard(anyLong())).thenReturn(board);
            when(memberService.findMemberById(anyLong())).thenReturn(normalMember);
            doNothing().when(memberValidator).throwIfEqual(any(), any());
            doNothing().when(memberValidator).throwIfBlind(any(), any(), any());
            doNothing().when(blockValidator).throwIfBlocked(any(), any(), any(), any());
            when(chatQueryService.findExistingChatroom(any(), any())).thenReturn(java.util.Optional.of(chatroom));
            when(chatCommandService.enterExistingChatroom(any(), any(), any())).thenReturn(memberChatroom);
            when(chatQueryService.getRecentChatSlice(any(), any())).thenReturn(null);
            when(chatResponseFactory.toChatMessageListResponse(any())).thenReturn(null);
            when(chatResponseFactory.toEnterChatroomResponse(any(), any(), anyString(), anyInt(), anyLong(), any())).thenReturn(null);

            // when
            chatFacadeService.startChatroomByBoardId(normalMember, 1L);

            // then
            verify(banValidator).throwIfBannedFromChatting(normalMember);
            // 제재 검증 통과 후 정상 로직이 실행되어야 함
            verify(boardService).findBoard(anyLong());
            verify(memberService).findMemberById(anyLong());
        }

        @Test
        @DisplayName("제재 검증은 다른 모든 검증보다 먼저 실행된다")
        void ban_validation_executes_before_other_validations() {
            // given
            doThrow(new MemberException(ErrorCode.MEMBER_BANNED_FROM_CHATTING))
                    .when(banValidator).throwIfBannedFromChatting(bannedMember);

            // when & then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(bannedMember, 1L))
                    .isInstanceOf(MemberException.class);

            // 제재 검증이 먼저 실행되고 실패하면 다른 검증은 실행되지 않아야 함
            verify(banValidator).throwIfBannedFromChatting(bannedMember);
            verify(boardService, never()).findBoard(anyLong());
            verify(memberValidator, never()).throwIfEqual(any(), any());
            verify(blockValidator, never()).throwIfBlocked(any(), any(), any(), any());
        }
    }

    // 헬퍼 메서드
    private Member createMember(BanType banType, LocalDateTime banExpireAt) {
        Member member = Member.createForGeneral(
                "test@example.com",
                "password",
                LoginType.GENERAL,
                "testUser",
                "KR1",
                Tier.BRONZE,
                1,
                50.0,
                Tier.BRONZE,
                1,
                50.0,
                10,
                5,
                true
        );

        if (banType != BanType.NONE) {
            member.applyBan(banType, banExpireAt);
        }

        // Set a default ID for test members
        ReflectionTestUtils.setField(member, "id", 3L);

        return member;
    }
}
