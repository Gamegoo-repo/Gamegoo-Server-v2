package com.gamegoo.gamegoo_v2.service.chat;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.domain.SystemMessageType;
import com.gamegoo.gamegoo_v2.chat.dto.request.SystemFlagRequest;
import com.gamegoo.gamegoo_v2.chat.repository.ChatRepository;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.service.ChatCommandService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.core.config.AsyncConfig;
import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.block.domain.Block;
import com.gamegoo.gamegoo_v2.social.block.repository.BlockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@Import(AsyncConfig.class)
class ChatCommandServiceTest {

    @Autowired
    private ChatCommandService chatCommandService;

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

    @MockitoSpyBean
    private MemberRepository memberRepository;

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
        chatRepository.deleteAllInBatch();
        memberChatroomRepository.deleteAllInBatch();
        chatroomRepository.deleteAllInBatch();
        blockRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("기존 채팅방 입장")
    class EnterExistingChatroomTest {

        @DisplayName("성공: 채팅방에서 퇴장한 상태인 경우 lastViewDate가 업데이트 되어야 한다.")
        @Test
        void enterExistingChatroomSucceedsWhenExited() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            MemberChatroom memberChatroom = chatCommandService.enterExistingChatroom(member, targetMember, chatroom);

            // then
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);
            assertThat(memberChatroom.getLastJoinDate()).isNull();
        }

        @DisplayName("성공: 채팅방에서 퇴장하지 않은 경우 상대가 나를 차단했어도 입장 가능하며 lastViewDate가 업데이트 되어야 한다.")
        @Test
        void enterExistingChatroomSucceedsWhenNotExitedAndBlockedByTarget() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, now.minusMinutes(10));
            createMemberChatroom(targetMember, chatroom, null);

            blockMember(targetMember, member);

            // when
            MemberChatroom memberChatroom = chatCommandService.enterExistingChatroom(member, targetMember, chatroom);

            // then
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);
            assertThat(memberChatroom.getLastJoinDate()).isCloseTo(now.minusMinutes(10), within(1, ChronoUnit.SECONDS));
        }

        @DisplayName("성공: 채팅방에서 퇴장하지 않은 경우 상대가 탈퇴했어도 입장 가능하며 lastViewDate가 업데이트 되어야 한다.")
        @Test
        void enterExistingChatroomSucceedsWhenNotExistedAndTargetIsBlind() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, now.minusMinutes(10));
            createMemberChatroom(targetMember, chatroom, null);

            // 상대 회원 탈퇴 처리
            blindMember(targetMember);

            // when
            MemberChatroom memberChatroom = chatCommandService.enterExistingChatroom(member, targetMember, chatroom);

            // then
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
            assertThat(memberChatroom.getLastViewDate()).isAfter(now);
            assertThat(memberChatroom.getLastJoinDate()).isCloseTo(now.minusMinutes(10), within(1, ChronoUnit.SECONDS));
        }

        @DisplayName("실패: 채팅방을 찾을 수 없거나 해당 회원의 채팅방이 아닌 경우 예외가 발생한다.")
        @Test
        void enterExistingChatroom_shouldThrownWhenChatroomNotFound() {
            // when // then
            assertThatThrownBy(() -> chatCommandService.enterExistingChatroom(member, targetMember, createChatroom()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHATROOM_ACCESS_DENIED.getMessage());
        }

        @DisplayName("실패: 채팅방을 퇴장한 상태이며 상대가 나를 차단한 경우 예외가 발생한다.")
        @Test
        void enterExistingChatroom_shouldThrownWhenMemberIsBlockedByTarget() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // 상대가 나를 차단
            blockMember(targetMember, member);

            // when // then
            assertThatThrownBy(() -> chatCommandService.enterExistingChatroom(member, targetMember, chatroom))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_BLOCKED_BY_TARGET.getMessage());
        }

        @DisplayName("실패: 채팅방을 퇴장한 상태이며 상대가 탈퇴한 경우 예외가 발생한다.")
        @Test
        void enterExistingChatroom_shouldThrownWhenTargetMemberIsBlind() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // 상대 회원 탈퇴 처리
            blindMember(targetMember);

            // when // then
            assertThatThrownBy(() -> chatCommandService.enterExistingChatroom(member, targetMember, chatroom))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_TARGET_DEACTIVATED.getMessage());
        }

    }

    @Nested
    @DisplayName("새로운 채팅방 생성")
    class CreateChatroomTest {

        @DisplayName("새로운 채팅방 생성 성공: chatroom과 memberChatroom 엔티티가 저장되어야 한다.")
        @Test
        void createChatroomSucceeds() {
            // when
            Chatroom chatroom = chatCommandService.createChatroom(member, targetMember);

            // then
            assertThat(chatroom).isNotNull();
            assertThat(memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(), chatroom.getId()))
                    .isPresent();
            assertThat(memberChatroomRepository.findByMemberIdAndChatroomId(targetMember.getId(), chatroom.getId()))
                    .isPresent();
        }

    }

    @Nested
    @DisplayName("시스템 메시지 생성")
    class CreateSystemChatTest {

        @DisplayName("실패: 해당 게시글을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void createSystemChat_shouldThrownWhenBoardNotFound() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            SystemFlagRequest request = SystemFlagRequest.builder()
                    .boardId(10000L)
                    .flag(1)
                    .build();

            // when // then
            assertThatThrownBy(() -> chatCommandService.createSystemChat(request, member, targetMember, chatroom))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.ADD_BOARD_SYSTEM_CHAT_FAILED.getMessage());
        }

        @DisplayName("실패: system member를 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void createSystemChat_shouldThrownWhenSystemMemberIsNotFound() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            Board board = createBoard(targetMember);

            SystemFlagRequest request = SystemFlagRequest.builder()
                    .boardId(board.getId())
                    .flag(1)
                    .build();

            // when // then
            assertThatThrownBy(() -> chatCommandService.createSystemChat(request, member, targetMember, chatroom))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.SYSTEM_MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("성공: 새로운 시스템 메시지가 생성 및 저장되어야 한다.")
        @Test
        void createSystemChatSucceeds() {
            // given
            Member systemMember = createMember("sytemMember@gmail.com", "systemMember");
            given(memberRepository.findById(0L)).willReturn(Optional.of(systemMember));

            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            Board board = createBoard(targetMember);

            SystemFlagRequest request = SystemFlagRequest.builder()
                    .boardId(board.getId())
                    .flag(1)
                    .build();

            // when
            List<Chat> chats = chatCommandService.createSystemChat(request, member, targetMember, chatroom);

            // then
            // 시스템 메시지가 저장 되었는지 검증
            List<Chat> systemMessages = chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(),
                    systemMember.getId());
            assertThat(systemMessages).hasSize(2);

            Chat memberSystemMessage = chats.get(0);
            Chat targetSystemMessage = chats.get(1);
            assertThat(memberSystemMessage.getContents()).isEqualTo(SystemMessageType.of(1).getMessage());
            assertThat(targetSystemMessage.getContents()).isEqualTo(SystemMessageType.of(3).getMessage());
        }

    }

    @Nested
    @DisplayName("lastViewDate 업데이트")
    class UpdateLastViewDateTest {

        @DisplayName("성공: lastViewDate가 변경되어야 한다.")
        @Test
        void updateLastViewDateSucceeds() {
            // given
            Chatroom chatroom = createChatroom();
            LocalDateTime lastJoinDate = LocalDateTime.now().minusMinutes(10);
            createMemberChatroom(member, chatroom, lastJoinDate);
            createMemberChatroom(targetMember, chatroom, lastJoinDate);

            Chat chat = createChat(member, "message", chatroom);

            // when
            MemberChatroom memberChatroom = chatCommandService.updateLastViewDate(member, chatroom,
                    chat.getCreatedAt());

            // then
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
            assertThat(memberChatroom.getLastViewDate()).isCloseTo(chat.getCreatedAt(), within(1, ChronoUnit.SECONDS));
            assertThat(memberChatroom.getLastJoinDate()).isCloseTo(lastJoinDate, within(1, ChronoUnit.SECONDS));
        }

    }

    @Nested
    @DisplayName("두 회원의 lastJoinDate 업데이트")
    class UpdateLastJoinDatesTest {

        @DisplayName("성공: member와 targetMember의 기존 lastJoinDate가 null인 경우, 두 lastJoinDate 모두 업데이트 되어야 한다.")
        @Test
        void updateLastJoinDatesSucceedsWhenLastJoinDatesAreNull() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            LocalDateTime updateDate = LocalDateTime.now().plusMinutes(10);

            // when
            chatCommandService.updateLastJoinDates(member, targetMember, updateDate, updateDate, chatroom);

            // then
            // lastJoinDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    targetMember.getId(), chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastJoinDate()).isNotNull();
            assertThat(memberChatroom.getLastJoinDate()).isCloseTo(updateDate, within(1, ChronoUnit.SECONDS));
            assertThat(targetMemberChatroom.getLastJoinDate()).isNotNull();
            assertThat(targetMemberChatroom.getLastJoinDate()).isCloseTo(updateDate, within(1, ChronoUnit.SECONDS));

            // event 발생 여부 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(socketService, times(2)).joinSocketToChatroom(any(Long.class), any(String.class));
            });
        }

        @DisplayName("성공: member의 lastJoinDate만 null인 경우, member의 lastJoinDate만 업데이트 되어야 한다.")
        @Test
        void updateLastJoinDatesSucceedsWhenMemberLastJoinDateIsNull() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, now);

            LocalDateTime updateDate = now.plusMinutes(10);

            // when
            chatCommandService.updateLastJoinDates(member, targetMember, updateDate, updateDate, chatroom);

            // then
            // lastJoinDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    targetMember.getId(), chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastJoinDate()).isNotNull();
            assertThat(memberChatroom.getLastJoinDate()).isCloseTo(updateDate, within(1, ChronoUnit.SECONDS));
            assertThat(targetMemberChatroom.getLastJoinDate()).isCloseTo(now, within(1, ChronoUnit.SECONDS));

            // event 발생 여부 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(socketService, times(1)).joinSocketToChatroom(any(Long.class), any(String.class));
            });
        }

    }

    @Nested
    @DisplayName("새로운 채팅 등록 시 lastJoinDate, lastViewDate 업데이트")
    class UpdateMemberChatroomDatesByAddChatTest {

        @DisplayName("성공: 두 회원의 lastJoinDate가 null인 경우 member의 lastViewDate가 업데이트 되어야 하고, 두 회원의 lastJoinDate가 업데이트 " +
                "되어야 한다.")
        @Test
        void updateMemberChatroomDatesByAddChatSucceedsWhenLastJoinDatesAreNull() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);
            Chat chat = createChat(member, "message", chatroom);

            // when
            chatCommandService.updateMemberChatroomDatesByAddChat(member, targetMember, chat);

            // then
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    targetMember.getId(), chatroom.getId()).orElseThrow();

            LocalDateTime createdAt = chat.getCreatedAt();

            // lastViewDate 업데이트 검증
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
            assertThat(memberChatroom.getLastViewDate()).isCloseTo(createdAt, within(1, ChronoUnit.SECONDS));

            // lastJoinDate 업데이트 검증
            assertThat(memberChatroom.getLastJoinDate()).isNotNull();
            assertThat(memberChatroom.getLastJoinDate()).isCloseTo(createdAt, within(1, ChronoUnit.SECONDS));
            assertThat(targetMemberChatroom.getLastJoinDate()).isNotNull();
            assertThat(targetMemberChatroom.getLastJoinDate()).isCloseTo(createdAt, within(1, ChronoUnit.SECONDS));

            // event 발생 여부 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(socketService, times(2)).joinSocketToChatroom(any(Long.class), any(String.class));
            });
        }

        @DisplayName("성공: targetMember의 lastJoinDate만 null인 경우, member의 lastViewDate와 targetMember의 lastJoinDate만 " +
                "업데이트 되어야 한다.")
        @Test
        void updateMemberChatroomDatesByAddChatSucceedsWhenTargetMemberLastJoinDateIsNull() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, now);
            createMemberChatroom(targetMember, chatroom, null);
            Chat chat = createChat(member, "message", chatroom);

            // when
            chatCommandService.updateMemberChatroomDatesByAddChat(member, targetMember, chat);

            // then
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    targetMember.getId(), chatroom.getId()).orElseThrow();

            // lastViewDate 업데이트 검증
            assertThat(memberChatroom.getLastViewDate()).isNotNull();
            assertThat(memberChatroom.getLastViewDate()).isCloseTo(chat.getCreatedAt(), within(1, ChronoUnit.SECONDS));

            // lastJoinDate 업데이트 검증
            assertThat(targetMemberChatroom.getLastJoinDate()).isNotNull();
            assertThat(targetMemberChatroom.getLastJoinDate()).isCloseTo(chat.getCreatedAt(),
                    within(1, ChronoUnit.SECONDS));
            assertThat(memberChatroom.getLastJoinDate()).isCloseTo(now, within(1, ChronoUnit.SECONDS));

            // event 발생 여부 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(socketService, times(1)).joinSocketToChatroom(any(Long.class), any(String.class));
            });
        }

    }

    private Member createMember(String email, String gameName) {
        return memberRepository.save(Member.builder()
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
                .build());
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

    private Board createBoard(Member member) {
        return boardRepository.save(Board.builder()
                .member(member)
                .gameMode(GameMode.FAST)
                .mainP(Position.ADC)
                .subP(Position.JUNGLE)
                .wantP(Position.ADC)
                .mike(Mike.AVAILABLE)
                .content("content")
                .boardProfileImage(1)
                .build());
    }

    private Chat createChat(Member fromMember, String contents, Chatroom chatroom) {
        return chatRepository.save(Chat.builder()
                .fromMember(fromMember)
                .contents(contents)
                .chatroom(chatroom)
                .build());
    }


    private Block blockMember(Member member, Member targetMember) {
        return blockRepository.save(Block.create(member, targetMember));
    }

    private void blindMember(Member member) {
        member.updateBlind(true);
        memberRepository.save(member);
    }

}
