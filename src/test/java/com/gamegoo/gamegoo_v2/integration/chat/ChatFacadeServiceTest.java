package com.gamegoo.gamegoo_v2.integration.chat;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.dto.request.ChatCreateRequest;
import com.gamegoo.gamegoo_v2.chat.dto.request.SystemFlagRequest;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.EnterChatroomResponse;
import com.gamegoo.gamegoo_v2.chat.repository.ChatRepository;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.service.ChatFacadeService;
import com.gamegoo.gamegoo_v2.chat.service.ChatQueryService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.block.domain.Block;
import com.gamegoo.gamegoo_v2.social.block.repository.BlockRepository;
import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import com.gamegoo.gamegoo_v2.social.friend.domain.FriendRequest;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRepository;
import com.gamegoo.gamegoo_v2.social.friend.repository.FriendRequestRepository;
import com.gamegoo.gamegoo_v2.utils.TimestampUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
class ChatFacadeServiceTest {

    @Autowired
    private ChatFacadeService chatFacadeService;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private MemberRecentStatsRepository memberRecentStatsRepository;

    @MockitoSpyBean
    private MemberRepository memberRepository;

    @MockitoSpyBean
    private MemberService memberService;

    @MockitoSpyBean
    private ChatQueryService chatQueryService;

    @MockitoBean
    private SocketService socketService;

    private Member member;
    private Member targetMember;


    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
        targetMember = createMember("target@gmail.com", "targetMember");
    }

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAll();
        chatRepository.deleteAllInBatch();
        memberChatroomRepository.deleteAllInBatch();
        chatroomRepository.deleteAllInBatch();
        blockRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        friendRequestRepository.deleteAllInBatch();
        friendRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("특정 회원과 채팅 시작")
    class StartChatroomByMemberIdTest {

        @DisplayName("실패: 상대 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void startChatroomByMemberId_shouldThrownWhenTargetMemberNotFound() {
            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByMemberId(member, 10000L))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 대상 회원으로 본인 id를 입력한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMemberId_shouldThrownWhenTargetMemberIsSelf() {
            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByMemberId(member, member.getId()))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(ErrorCode._BAD_REQUEST.getMessage());
        }

        @DisplayName("실패: 내가 상대를 차단한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMemberId_shouldThrownWhenTargetMemberIsBlockedByMe() {
            // given
            blockMember(member, targetMember);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByMemberId(member, targetMember.getId()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_TARGET_IS_BLOCKED.getMessage());
        }

        @DisplayName("성공: 기존 채팅방이 존재히는 경우 해당 채팅방에 입장 처리 및 최근 메시지 내역을 조회해야 한다.")
        @Test
        void startChatroomByMemberIdSucceedsWhenExistingChatroom() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            EnterChatroomResponse response = chatFacadeService.startChatroomByMemberId(member, targetMember.getId());

            // then
            // lastViewDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);

            // response 검증
            assertEnterChatroomResponse(response, chatroom, targetMember);
            assertThat(response.getSystem()).isNull();
        }

        @DisplayName("성공: 기존 채팅방이 존재하지 않는 경우 새 채팅방 생성 후 입장 처리되어야 한다.")
        @Test
        void startChatroomByMemberIdSucceedsWhenNoExistingChatroom() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // when
            EnterChatroomResponse response = chatFacadeService.startChatroomByMemberId(member, targetMember.getId());

            // then
            // 생성된 엔티티 검증
            Chatroom chatroom = chatroomRepository.findChatroomByMemberIds(member.getId(), targetMember.getId())
                    .orElseThrow();
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();

            assertThat(memberChatroom.getLastViewDate()).isAfter(now);
            assertThat(memberChatroom.getLastJoinDate()).isNull();

            // response 검증
            assertEnterChatroomResponse(response, chatroom, targetMember);
            assertThat(response.getSystem()).isNull();
        }

        @DisplayName("실패: 기존 채팅방이 존재하지 않으며 상대가 나를 차단한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMemberId_shouldThrownWhenNoExistingChatroomAndBlockedByTarget() {
            // given
            blockMember(targetMember, member);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByMemberId(member, targetMember.getId()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_BLOCKED_BY_TARGET.getMessage());
        }

        @DisplayName("실패: 기존 채팅방이 존재하지 않으며 상대가 탈퇴한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMemberId_shouldThrownWhenNoExistingChatroomAndTargetIsBlind() {
            // given
            blindMember(targetMember);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByMemberId(member, targetMember.getId()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_TARGET_DEACTIVATED.getMessage());
        }

    }

    @Nested
    @DisplayName("특정 글을 통한 채팅방 시작")
    class StartChatroomByBoardIdTest {

        @DisplayName("실패: 해당 글을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void startChatroomByBoardId_shouldThrownWhenBoardNotFound() {
            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(member, 10000L))
                    .isInstanceOf(BoardException.class)
                    .hasMessage(ErrorCode.BOARD_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 게시글 작성자를 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void startChatroomByBoardId_shouldThrownWhenMemberNotFound() {
            // given
            Board board = createBoard(targetMember);

            willThrow(new MemberException(ErrorCode.MEMBER_NOT_FOUND))
                    .given(memberService).findMemberById(targetMember.getId());

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(member, board.getId()))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 게시글 작성자가 본인인 경우 예외가 발생한다.")
        @Test
        void startChatroomByBoardId_shouldThrownWhenWriterIsSelf() {
            // given
            Board board = createBoard(member);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(member, board.getId()))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(ErrorCode._BAD_REQUEST.getMessage());
        }

        @DisplayName("실패: 게시글 작성자가 탈퇴한 경우 예외가 발생한다.")
        @Test
        void startChatroomByBoardId_shouldThrownWhenWriterIsBlind() {
            // given
            Board board = createBoard(targetMember);

            blindMember(targetMember);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(member, board.getId()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_TARGET_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 상대가 나를 차단한 경우 예외가 발생한다.")
        @Test
        void startChatroomByBoardId_shouldThrownWhenNoExistingChatroomAndBlockedByTarget() {
            // given
            Board board = createBoard(targetMember);

            blockMember(targetMember, member);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(member, board.getId()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_BLOCKED_BY_TARGET.getMessage());
        }

        @DisplayName("실패: 내가 상대를 차단한 경우 예외가 발생한다.")
        @Test
        void startChatroomByBoardId_shouldThrownWhenTargetIsBlocked() {
            // given
            Board board = createBoard(targetMember);

            blockMember(member, targetMember);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.startChatroomByBoardId(member, board.getId()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_TARGET_IS_BLOCKED.getMessage());
        }

        @DisplayName("성공: 기존 채팅방에 퇴장한 상태인 경우 해당 채팅방에 입장 처리 및 최근 메시지 내역을 조회해야 한다. systemFlag로는 1을 반환해야 한다.")
        @Test
        void startChatroomByBoardIdSucceedsWhenExitedExistingChatroom() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            Board board = createBoard(targetMember);

            // when
            EnterChatroomResponse response = chatFacadeService.startChatroomByBoardId(member, board.getId());

            // then
            // lastViewDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);

            // response 검증
            assertEnterChatroomResponse(response, chatroom, targetMember);
            assertThat(response.getSystem().getFlag()).isEqualTo(1);
            assertThat(response.getSystem().getBoardId()).isEqualTo(board.getId());
        }

        @DisplayName("성공: 기존 채팅방에 입장한 상태인 경우 해당 채팅방에 입장 처리 및 최근 메시지 내역을 조회해야 한다. systemFlag로는 2를 반환해야 한다.")
        @Test
        void startChatroomByBoardIdSucceedsWhenExistingChatroom() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, now.minusDays(1));
            createMemberChatroom(targetMember, chatroom, null);

            Board board = createBoard(targetMember);

            // when
            EnterChatroomResponse response = chatFacadeService.startChatroomByBoardId(member, board.getId());

            // then
            // lastViewDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);

            // response 검증
            assertEnterChatroomResponse(response, chatroom, targetMember);
            assertThat(response.getSystem().getFlag()).isEqualTo(2);
            assertThat(response.getSystem().getBoardId()).isEqualTo(board.getId());
        }

        @DisplayName("성공: 기존 채팅방이 존재하지 않는 경우 새 채팅방 생성 후 입장 처리되어야 한다. systemFlag로는 1을 반환해야 한다.")
        @Test
        void startChatroomByBoardIdSucceedsWhenNoExistingChatroom() {
            // given
            LocalDateTime now = LocalDateTime.now();

            Board board = createBoard(targetMember);

            // when
            EnterChatroomResponse response = chatFacadeService.startChatroomByBoardId(member, board.getId());

            // then
            // lastViewDate 업데이트 검증
            Chatroom chatroom = chatroomRepository.findChatroomByMemberIds(member.getId(), targetMember.getId())
                    .orElseThrow();
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);

            // response 검증
            assertEnterChatroomResponse(response, chatroom, targetMember);
            assertThat(response.getSystem().getFlag()).isEqualTo(1);
            assertThat(response.getSystem().getBoardId()).isEqualTo(board.getId());
        }

    }

    @Nested
    @DisplayName("특정 채팅방 입장")
    class EnterChatroomTest {

        @DisplayName("실패: uuid에 해당하는 채팅방이 없는 경우 예외가 발생한다.")
        @Test
        void enterChatroom_shouldThrownWhenChatroomNotFound() {
            // when // then
            assertThatThrownBy(() -> chatFacadeService.enterChatroomByUuid(member, "notExistUuid"))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 내가 상대를 차단한 경우 예외가 발생한다.")
        @Test
        void enterChatroom_shouldThrownWhenTargetIsBlocked() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            blockMember(member, targetMember);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.enterChatroomByUuid(member, chatroom.getUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_TARGET_IS_BLOCKED.getMessage());
        }

        @DisplayName("실패: 해당 채팅방이 본인의 채팅방이 아닌 경우 예외가 발생한다.")
        @Test
        void enterChatroom_shouldThrownWhenAccessDenied() {
            // given
            Member otherMember = createMember("other@gmail.com", "otherMember");

            Chatroom chatroom = createChatroom();
            createMemberChatroom(otherMember, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.enterChatroomByUuid(member, chatroom.getUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage());
        }

        @DisplayName("성공: 채팅방에 입장 처리 및 최근 메시지 내역을 조회해야 한다.")
        @Test
        void enterChatroomSucceeds() {
            // given
            LocalDateTime now = LocalDateTime.now();

            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            EnterChatroomResponse response = chatFacadeService.enterChatroomByUuid(member, chatroom.getUuid());

            // then
            // lastViewDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);

            // response 검증
            assertEnterChatroomResponse(response, chatroom, targetMember);
            assertThat(response.getSystem()).isNull();
        }

    }

    @Nested
    @DisplayName("새로운 채팅 등록")
    class CreateChatTest {

        private Member systemMember;

        @BeforeEach
        void setUp() {
            systemMember = createMember("systemMember@gmail.com", "systemMember");
            given(memberRepository.findById(0L)).willReturn(Optional.of(systemMember));
        }

        @DisplayName("실패: 해당 채팅방을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void createChat_shouldThrownWhenChatroomNotFound() {
            // given
            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(null)
                    .build();

            // when // then
            assertThatThrownBy(() -> chatFacadeService.createChat(request, member.getId(), "no-uuid"))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 해당 채팅방이 회원의 것이 아닌 경우 예외가 발생한다.")
        @Test
        void createChat_shouldThrownWhenMemberChatroomNotExists() {
            // given
            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(null)
                    .build();

            // when // then
            assertThatThrownBy(() -> chatFacadeService.createChat(request, member.getId(), createChatroom().getUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage());
        }

        @DisplayName("실패: 상대가 탈퇴한 경우 예외가 발생한다.")
        @Test
        void createChat_shouldThrownWhenTargetIsBlind() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(null)
                    .build();

            blindMember(targetMember);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.createChat(request, member.getId(), chatroom.getUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_ADD_FAILED_TARGET_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 내가 상대를 차단한 경우 예외가 발생한다.")
        @Test
        void createChat_shouldThrownWhenTargetIsBlocked() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(null)
                    .build();

            blockMember(member, targetMember);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.createChat(request, member.getId(), chatroom.getUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_ADD_FAILED_TARGET_IS_BLOCKED.getMessage());
        }

        @DisplayName("실패: 상대가 나를 차단한 경우 예외가 발생한다.")
        @Test
        void createChat_shouldThrownWhenBlockedByTarget() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            ChatCreateRequest request = ChatCreateRequest.builder()
                    .message("message")
                    .system(null)
                    .build();

            // when
            blockMember(targetMember, member);

            // then
            assertThatThrownBy(() -> chatFacadeService.createChat(request, member.getId(), chatroom.getUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_ADD_FAILED_BLOCKED_BY_TARGET.getMessage());
        }

        @DisplayName("성공")
        @ParameterizedTest(name = "시스템 메시지: {0}, member 입장: {1}, targetMember 입장: {2}")
        @CsvSource({
                "true, true, true",  // 시스템 메시지 있음, member 입장 O, targetMember 입장 O
                "true, true, false", // 시스템 메시지 있음, member 입장 O, targetMember 입장 X
                "true, false, true", // 시스템 메시지 있음, member 입장 X, targetMember 입장 O
                "true, false, false",// 시스템 메시지 있음, member 입장 X, targetMember 입장 X
                "false, true, true", // 시스템 메시지 없음, member 입장 O, targetMember 입장 O
                "false, true, false",// 시스템 메시지 없음, member 입장 O, targetMember 입장 X
                "false, false, true",// 시스템 메시지 없음, member 입장 X, targetMember 입장 O
                "false, false, false"// 시스템 메시지 없음, member 입장 X, targetMember 입장 X
        })
        void createChatSucceeds(boolean systemMessage, boolean memberEntered, boolean targetEntered) {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, null);
            MemberChatroom targetMemberChatroom = createMemberChatroom(targetMember, chatroom, null);

            ChatCreateRequest request;

            // 조건에 따른 상태 설정
            if (systemMessage) {
                Board board = createBoard(targetMember);
                SystemFlagRequest systemFlagRequest = SystemFlagRequest.builder()
                        .boardId(board.getId())
                        .flag(1)
                        .build();

                request = ChatCreateRequest.builder()
                        .message("message")
                        .system(systemFlagRequest)
                        .build();
            } else {
                request = ChatCreateRequest.builder()
                        .message("message")
                        .system(null)
                        .build();
            }

            if (memberEntered) {
                memberChatroom.updateLastJoinDate(now);
            }

            if (targetEntered) {
                targetMemberChatroom.updateLastJoinDate(now);
            }

            // when
            chatFacadeService.createChat(request, member.getId(), chatroom.getUuid());

            // then
            // member의 lastViewDate 업데이트 검증
            memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);

            // 회원 메시지 저장 되었는지 검증
            List<Chat> chats = chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(), member.getId());
            assertThat(chats).hasSize(1);

            // chatroom의 lastChat, lastChatAt 업데이트 검증
            Chatroom updatedChatroom = chatroomRepository.findById(chatroom.getId()).orElseThrow();

            Chat lastChat = chats.get(chats.size() - 1);
            assertThat(updatedChatroom.getLastChatAt()).isCloseTo(lastChat.getCreatedAt(),
                    within(1, ChronoUnit.SECONDS));
            assertThat(updatedChatroom.getLastChatId()).isEqualTo(lastChat.getId());

            // 시스템 메시지 저장 되었는지 검증
            if (systemMessage) {
                List<Chat> systemChats = chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(),
                        systemMember.getId());
                assertThat(systemChats).hasSize(2);
            }

            // member의 lastJoinDate 업데이트 되었는지 검증
            if (!memberEntered) {
                memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                        chatroom.getId()).orElseThrow();
                assertThat(memberChatroom.getLastJoinDate()).isAfter(now);
            }

            // targetMember의 lastJoinDate 업데이트 되었는지 검증
            if (!targetEntered) {
                targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(targetMember.getId(),
                        chatroom.getId()).orElseThrow();
                assertThat(targetMemberChatroom.getLastJoinDate()).isAfter(now);
            }
        }

    }

    @Nested
    @DisplayName("채팅방 대회 내역 조회")
    class GetChatMessagesByCursorTest {

        @DisplayName("실패: uuid에 해당하는 채팅방이 없는 경우 예외가 발생한다.")
        @Test
        void getChatMessagesByCursor_shouldThrownWhenChatroomNotFound() {
            // when // then
            assertThatThrownBy(() -> chatFacadeService.getChatMessagesByCursor(member, "no-uuid", null))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 해당 채팅방이 본인의 채팅방이 아닌 경우 예외가 발생한다.")
        @Test
        void getChatMessagesByCursor_shouldThrownWhenMemberChatroomNotExists() {
            // given
            Member otherMember = createMember("other@gmail.com", "otherMember");

            Chatroom chatroom = createChatroom();
            createMemberChatroom(otherMember, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.getChatMessagesByCursor(member, chatroom.getUuid(), null))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage());
        }

        @DisplayName("성공: cursor가 null인 경우")
        @Test
        void getChatMessagesByCursorSucceedsWhenCursorIsNull() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            ChatMessageListResponse response = chatFacadeService.getChatMessagesByCursor(member, chatroom.getUuid(),
                    null);

            // then
            verify(chatQueryService, Mockito.times(1))
                    .getRecentChatSlice(any(Member.class), any(Chatroom.class));

            assertThat(response.getChatMessageList()).isEmpty();
            assertThat(response.getListSize()).isEqualTo(0);
            assertThat(response.getNextCursor()).isNull();
            assertThat(response.getHasNext()).isFalse();
        }

        @DisplayName("성공: cursor가 null이 아닌 경우")
        @Test
        void getChatMessagesByCursorSucceedsWhenCursorIsNotNull() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            ChatMessageListResponse response = chatFacadeService.getChatMessagesByCursor(member, chatroom.getUuid(),
                    1L);

            // then
            verify(chatQueryService, Mockito.times(1))
                    .getChatSliceByCursor(any(Member.class), any(Chatroom.class), any(Long.class));

            assertThat(response.getChatMessageList()).isEmpty();
            assertThat(response.getListSize()).isEqualTo(0);
            assertThat(response.getNextCursor()).isNull();
            assertThat(response.getHasNext()).isFalse();
        }

    }

    @Nested
    @DisplayName("안읽은 채팅방 uuid 조회")
    class GetUnreadChatroomUuidsTest {

        @DisplayName("성공: 입장 상태인 채팅방이 없는 경우 빈 list를 반환한다.")
        @Test
        void getUnreadChatroomUuidsSucceedsWithEmptyList() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            List<String> result = chatFacadeService.getUnreadChatroomUuids(member);

            // then
            assertThat(result).isEmpty();
        }

        @DisplayName("성공: 입장 상태인 채팅방이 있는 경우 해당 채팅방의 안읽은 메시지 조회 메소드를 호출해야 한다.")
        @Test
        void getUnreadChatroomUuidsSucceeds() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, LocalDateTime.now());
            createMemberChatroom(targetMember, chatroom, null);

            // when
            List<String> result = chatFacadeService.getUnreadChatroomUuids(member);

            // then
            verify(chatQueryService, Mockito.times(1))
                    .countUnreadChats(any(Member.class), any(Chatroom.class));

            assertThat(result).isEmpty();
        }

    }

    @Nested
    @DisplayName("채팅방 읽음 처리")
    class ReadChatMessageTest {

        @DisplayName("실패: uuid에 해당하는 채팅방이 없는 경우 예외가 발생한다.")
        @Test
        void readChatMessage_shouldThrownWhenChatroomNotFound() {
            // when // then
            assertThatThrownBy(() -> chatFacadeService.readChatMessage(member, "no-uuid", null))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());

        }

        @DisplayName("실패: 해당 채팅방이 본인의 채팅방이 아닌 경우 예외가 발생한다.")
        @Test
        void readChatMessage_shouldThrownWhenMemberChatroomNotExists() {
            // given
            Member otherMember = createMember("other@gmail.com", "otherMember");

            Chatroom chatroom = createChatroom();
            createMemberChatroom(otherMember, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.readChatMessage(member, chatroom.getUuid(), null))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage());
        }

        @DisplayName("실패: 해당 채팅방에 입장한 상태가 아닌 경우 예외가 발생한다.")
        @Test
        void readChatMessage_shouldThrownWhenExitedChatroom() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.readChatMessage(member, chatroom.getUuid(), null))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_READ_FAILED_NOT_ENTERED_CHATROOM.getMessage());
        }

        @DisplayName("실패: timestamp에 해당하는 채팅 메시지가 없는 경우 예외가 발생한다.")
        @Test
        void readChatMessage_shouldThrownWhenMessageNotFound() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, LocalDateTime.now().minusSeconds(1));
            createMemberChatroom(targetMember, chatroom, null);

            // when // thena
            assertThatThrownBy(() -> chatFacadeService.readChatMessage(member, chatroom.getUuid(), 1L))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_MESSAGE_NOT_FOUND.getMessage());
        }

        @DisplayName("성공: timestamp가 null인 경우 현재 시각으로 lastViewDate를 업데이트 한다.")
        @Test
        void readChatMessageSucceedsWhenTimestampIsNull() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, LocalDateTime.now().minusDays(1));
            createMemberChatroom(targetMember, chatroom, null);

            // when
            String result = chatFacadeService.readChatMessage(member, chatroom.getUuid(), null);

            // then
            assertThat(result).isEqualTo("채팅 메시지 읽음 처리 성공");

            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member.getId(), chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
        }

        @DisplayName("성공: timestamp를 정상 입력한 경우 해당 채팅의 createdAt으로 lastViewDate를 업데이트 한다.")
        @Test
        void readChatMessageSucceeds() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, LocalDateTime.now().minusDays(1));
            createMemberChatroom(targetMember, chatroom, null);

            Chat chat = createChat(member, "message 1", chatroom);

            // when
            String result = chatFacadeService.readChatMessage(member, chatroom.getUuid(), chat.getTimestamp());

            // then
            assertThat(result).isEqualTo("채팅 메시지 읽음 처리 성공");

            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member.getId(), chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
            assertThat(memberChatroom.getLastViewDate()).isCloseTo(chat.getCreatedAt(), within(1, ChronoUnit.MILLIS));
        }

    }

    @Nested
    @DisplayName("채팅방 나가기")
    class ExitChatroomTest {

        @DisplayName("실패: uuid에 해당하는 채팅방이 없는 경우 예외가 발생한다. ")
        @Test
        void exitChatroom_shouldThrownWhenChatroomNotFound() {
            // when // then
            assertThatThrownBy(() -> chatFacadeService.exitChatroom(member, "no-uuid"))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 해당 채팅방이 본인의 채팅방이 아닌 경우 예외가 발생한다.")
        @Test
        void exitChatroom_shouldThrownWhenMemberChatroomNotExists() {
            // given
            Member otherMember = createMember("other@gmail.com", "otherMember");

            Chatroom chatroom = createChatroom();
            createMemberChatroom(otherMember, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when // then
            assertThatThrownBy(() -> chatFacadeService.exitChatroom(member, chatroom.getUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage());

        }

        @DisplayName("성공: lastJoinDate가 null로 업데이트 되어야 한다.")
        @Test
        void exitChatroomSucceeds() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, LocalDateTime.now().minusDays(1));
            createMemberChatroom(targetMember, chatroom, null);

            // when
            String result = chatFacadeService.exitChatroom(member, chatroom.getUuid());

            // then
            assertThat(result).isEqualTo("채팅방 나가기 성공");

            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastJoinDate()).isNull();
        }

    }

    @Nested
    @DisplayName("채팅방 목록 조회")
    class GetChatroomsTest {

        @DisplayName("성공: 입장 상태인 채팅방이 없는 경우")
        @Test
        void getChatroomsSucceedsWhenNoActiveChatroom() {
            // when
            ChatroomListResponse response = chatFacadeService.getChatrooms(member);

            // then
            assertThat(response.getChatroomResponseList()).isEmpty();
            assertThat(response.getListSize()).isEqualTo(0);
        }

        @DisplayName("성공")
        @Test
        void getChatroomsSucceeds() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // targetMember1 생성
            Member targetMember1 = createMember("targetMember1@gmail.com", "targetMember1");
            Chatroom chatroom1 = createChatroom();
            createMemberChatroom(targetMember1, chatroom1, now, now);
            createMemberChatroom(member, chatroom1, now, now);
            Chat chat1 = createChat(targetMember1, "message 1", chatroom1);
            updateLastChat(chatroom1, chat1);

            friendRepository.save(Friend.create(member, targetMember1));
            friendRepository.save(Friend.create(targetMember1, member));

            // targetMember2 생성
            Member targetMember2 = createMember("targetmember2@gmail.com", "targetMember2");
            Chatroom chatroom2 = createChatroom();
            createMemberChatroom(member, chatroom2, now.plusHours(1), now);
            createMemberChatroom(targetMember2, chatroom2, now.plusHours(1), now);
            Chat chat2 = createChat(targetMember2, "message 2", chatroom2);
            updateLastChat(chatroom2, chat2);

            friendRequestRepository.save(FriendRequest.create(member, targetMember2));

            // targetMember3 생성
            Member targetMember3 = createMember("targetmember3@gmail.com", "targetMember3");
            Chatroom chatroom3 = createChatroom();
            createMemberChatroom(member, chatroom3, now, now);
            createMemberChatroom(targetMember3, chatroom3, now, now);
            Chat chat3 = createChat(targetMember3, "message 3", chatroom3);
            updateLastChat(chatroom3, chat3);

            blockMember(targetMember3, member);

            // targetMember4 생성
            Member targetMember4 = createMember("targetmember4@gmail.com", "targetMember4");
            Chatroom chatroom4 = createChatroom();
            createMemberChatroom(member, chatroom4, null);
            createMemberChatroom(targetMember4, chatroom4, now);

            // when
            ChatroomListResponse response = chatFacadeService.getChatrooms(member);

            // then
            assertThat(response.getChatroomResponseList()).hasSize(3);
            assertThat(response.getListSize()).isEqualTo(3);

            ChatroomResponse chatroomResponse1 = response.getChatroomResponseList().get(0);
            ChatroomResponse chatroomResponse2 = response.getChatroomResponseList().get(1);
            ChatroomResponse chatroomResponse3 = response.getChatroomResponseList().get(2);

            assertThat(chatroomResponse1.getChatroomId()).isEqualTo(chatroom3.getId());
            assertThat(chatroomResponse1.getLastMsg()).isEqualTo(chat3.getContents());
            assertThat(chatroomResponse1.getLastMsgTimestamp()).isEqualTo(chat3.getTimestamp());
            assertThat(chatroomResponse1.getNotReadMsgCnt()).isEqualTo(1);
            assertThat(chatroomResponse1.isBlocked()).isTrue();

            assertThat(chatroomResponse2.getChatroomId()).isEqualTo(chatroom2.getId());
            assertThat(chatroomResponse2.getLastMsg()).isEqualTo(chat2.getContents());
            assertThat(chatroomResponse2.getLastMsgTimestamp()).isEqualTo(chat2.getTimestamp());
            assertThat(chatroomResponse2.getNotReadMsgCnt()).isEqualTo(0);
            assertThat(chatroomResponse2.getFriendRequestMemberId()).isEqualTo(member.getId());

            assertThat(chatroomResponse3.getChatroomId()).isEqualTo(chatroom1.getId());
            assertThat(chatroomResponse3.getLastMsg()).isEqualTo(chat1.getContents());
            assertThat(chatroomResponse3.getLastMsgTimestamp()).isEqualTo(chat1.getTimestamp());
            assertThat(chatroomResponse3.getNotReadMsgCnt()).isEqualTo(1);
            assertThat(chatroomResponse3.isFriend()).isTrue();
        }

    }

    @Nested
    @DisplayName("모든 채팅방 uuid 조회")
    class GetChatroomUuidsTest {

        @DisplayName("성공: 회원의 채팅방이 없는 경우 빈 리스트를 반환한다.")
        @Test
        void getChatroomUuidsSucceedsWhenNoChatroom() {
            // when
            List<String> chatroomUuids = chatFacadeService.getChatroomUuids(member.getId());

            // then
            assertThat(chatroomUuids).isEmpty();
        }

        @DisplayName("성공: 채팅방 uuid 리스트를 반환한다.")
        @Test
        void getChatroomUuidsSucceeds() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // targetMember1 생성
            Member targetMember1 = createMember("targetMember1@gmail.com", "targetMember1");
            Chatroom chatroom1 = createChatroom();
            createMemberChatroom(targetMember1, chatroom1, now, now);
            createMemberChatroom(member, chatroom1, now, now);

            // targetMember2 생성
            Member targetMember2 = createMember("targetmember2@gmail.com", "targetMember2");
            Chatroom chatroom2 = createChatroom();
            createMemberChatroom(member, chatroom2, now, now);
            createMemberChatroom(targetMember2, chatroom2, now, now);

            // targetMember3 생성
            Member targetMember3 = createMember("targetmember3@gmail.com", "targetMember3");
            Chatroom chatroom3 = createChatroom();
            createMemberChatroom(member, chatroom3, null, null);
            createMemberChatroom(targetMember3, chatroom3, null, null);

            // when
            List<String> chatroomUuids = chatFacadeService.getChatroomUuids(member.getId());

            // then
            assertThat(chatroomUuids).hasSize(2);
        }

    }

    private void assertEnterChatroomResponse(EnterChatroomResponse response, Chatroom chatroom, Member targetMember) {
        assertThat(response.getUuid()).isEqualTo(chatroom.getUuid());
        assertThat(response.getMemberId()).isEqualTo(targetMember.getId());
        assertThat(response.getGameName()).isEqualTo(targetMember.getGameName());
        assertThat(response.getMemberProfileImg()).isEqualTo(targetMember.getProfileImage());
        assertThat(response.isFriend()).isFalse();
        assertThat(response.isBlind()).isFalse();
        assertThat(response.isBlocked()).isFalse();
        assertThat(response.getFriendRequestMemberId()).isNull();
        assertThat(response.getChatMessageListResponse().getChatMessageList()).isEmpty();
    }

    private Member createMember(String email, String gameName) {
        Member member = Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag("TAG")
                .soloTier(Tier.IRON)
                .soloRank(0)
                .soloWinRate(0.0)
                .soloGameCount(0)
                .freeTier(Tier.IRON)
                .freeRank(0)
                .freeWinRate(0.0)
                .freeGameCount(0)
                .isAgree(true)
                .build();

        memberRecentStatsRepository.save(MemberRecentStats.builder()
                .member(member)
                .build());

        return memberRepository.save(member);
    }

    private Chatroom createChatroom() {
        return chatroomRepository.save(Chatroom.builder()
                .uuid(UUID.randomUUID().toString())
                .build());
    }

    private MemberChatroom createMemberChatroom(Member member, Chatroom chatroom, LocalDateTime lastJoinDate) {
        return memberChatroomRepository.save(MemberChatroom.builder()
                .chatroom(chatroom)
                .member(member)
                .lastViewDate(null)
                .lastJoinDate(lastJoinDate)
                .build());
    }

    private MemberChatroom createMemberChatroom(Member member, Chatroom chatroom, LocalDateTime lastViewDate,
                                                LocalDateTime lastJoinDate) {
        return memberChatroomRepository.save(MemberChatroom.builder()
                .chatroom(chatroom)
                .member(member)
                .lastViewDate(lastViewDate)
                .lastJoinDate(lastJoinDate)
                .build());
    }

    private Board createBoard(Member member) {
        return boardRepository.save(Board.builder()
                .member(member)
                .gameMode(GameMode.SOLO)
                .mainP(Position.ADC)
                .subP(Position.JUNGLE)
                .wantP(List.of(Position.ADC))
                .mike(Mike.AVAILABLE)
                .content("content")
                .boardProfileImage(1)
                .build());
    }

    private Chat createChat(Member member, String content, Chatroom chatroom) {
        return chatRepository.save(Chat.builder()
                .contents(content)
                .systemType(null)
                .chatroom(chatroom)
                .fromMember(member)
                .toMember(null)
                .sourceBoard(null)
                .timestamp(TimestampUtil.getNowUtcTimeStamp())
                .build());
    }

    private Block blockMember(Member member, Member targetMember) {
        return blockRepository.save(Block.create(member, targetMember));
    }

    private void blindMember(Member member) {
        member.updateBlind(true);
        memberRepository.save(member);
    }

    private void updateLastChat(Chatroom chatroom, Chat chat) {
        chatroom.updateLastChatId(chat.getId());
        chatroom.updateLastChatAt(chat.getCreatedAt());
        chatroomRepository.save(chatroom);
    }

}
