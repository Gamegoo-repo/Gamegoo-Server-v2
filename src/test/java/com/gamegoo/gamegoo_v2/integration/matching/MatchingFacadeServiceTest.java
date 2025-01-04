package com.gamegoo.gamegoo_v2.integration.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.domain.SystemMessageType;
import com.gamegoo.gamegoo_v2.chat.repository.ChatRepository;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.matching.service.MatchingFacadeService;
import com.gamegoo.gamegoo_v2.social.block.domain.Block;
import com.gamegoo.gamegoo_v2.social.block.repository.BlockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
public class MatchingFacadeServiceTest {

    @MockitoSpyBean
    private MemberRepository memberRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MatchingFacadeService matchingFacadeService;

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
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("매칭을 통한 채팅방 시작")
    class StartChatroomByMatchingTest {

        @DisplayName("실패: 두 회원이 동일한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMatching_shouldThrownWhenTargetMemberIsSelf() {
            // when // then
            assertThatThrownBy(() -> matchingFacadeService.startChatroomByMatching(member, member))
                    .isInstanceOf(GlobalException.class);
        }

        @DisplayName("실패: 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMatching_shouldThrownWhenMemberIsBlind() {
            // given
            blindMember(targetMember);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.startChatroomByMatching(member, targetMember))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 상대 회원을 차단한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMatching_shouldThrownWhenTargetIsBlocked() {
            // given
            blockMember(member, targetMember);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.startChatroomByMatching(member, targetMember))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_TARGET_IS_BLOCKED.getMessage());
        }

        @DisplayName("실패: 상대 회원에게 차단 당한 경우 예외가 발생한다.")
        @Test
        void startChatroomByMatching_shouldThrownWhenBlockedByTarget() {
            // given
            blockMember(targetMember, member);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.startChatroomByMatching(member, targetMember))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.CHAT_START_FAILED_BLOCKED_BY_TARGET.getMessage());
        }

        @DisplayName("성공: 기존 채팅방이 존재하는 경우")
        @Test
        void startChatroomByMatchingSucceedsWhenChatroomExists() {
            // given
            Chatroom chatroom = createChatroom();

            LocalDateTime lastJoinDate = LocalDateTime.now().minusDays(1);
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, lastJoinDate);

            Member systemMember = createMember("systemMember@gmail.com", "systemMember");
            given(memberRepository.findById(0L)).willReturn(Optional.of(systemMember));

            // when
            String result = matchingFacadeService.startChatroomByMatching(member, targetMember);

            // then
            assertThat(result).isEqualTo(chatroom.getUuid());

            // member의 lastJoinDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastJoinDate()).isNotNull();

            // socket service 호출 여부 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(socketService, times(1)).joinSocketToChatroom(eq(member.getId()), eq(chatroom.getUuid()));
            });

            // targetMember의 lastJoinDate 검증
            MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    targetMember.getId(), chatroom.getId()).orElseThrow();
            assertThat(targetMemberChatroom.getLastJoinDate()).isCloseTo(lastJoinDate, within(1, ChronoUnit.SECONDS));

            // 매칭 시스템 메시지 생성 검증
            List<Chat> systemChats = chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(),
                    systemMember.getId());
            assertThat(systemChats).hasSize(2);
            systemChats.forEach(systemChat -> {
                assertThat(systemChat.getContents()).isEqualTo(SystemMessageType.MATCH_SUCCESS_MESSAGE.getMessage());
            });
        }

        @DisplayName("성공: 기존 채팅방이 존재하지 않는 경우")
        @Test
        void startChatroomByMatchingSucceedsWhenChatroomNotExists() {
            // given
            Member systemMember = createMember("systemMember@gmail.com", "systemMember");
            given(memberRepository.findById(0L)).willReturn(Optional.of(systemMember));

            // when
            String result = matchingFacadeService.startChatroomByMatching(member, targetMember);

            // then
            Chatroom chatroom = chatroomRepository.findByUuid(result).orElseThrow();

            // lastJoinDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    chatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastJoinDate()).isNotNull();

            MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    targetMember.getId(), chatroom.getId()).orElseThrow();
            assertThat(targetMemberChatroom.getLastJoinDate()).isNotNull();

            // socket service 호출 여부 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(socketService, times(2)).joinSocketToChatroom(any(Long.class), eq(chatroom.getUuid()));
            });

            // 매칭 시스템 메시지 생성 검증
            List<Chat> systemChats = chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(),
                    systemMember.getId());
            assertThat(systemChats).hasSize(2);
            systemChats.forEach(systemChat -> {
                assertThat(systemChat.getContents()).isEqualTo(SystemMessageType.MATCH_SUCCESS_MESSAGE.getMessage());
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
                .tier(Tier.IRON)
                .gameRank(0)
                .winRate(0.0)
                .gameCount(0)
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

    private void blindMember(Member member) {
        member.updateBlind(true);
        memberRepository.save(member);
    }

    private Block blockMember(Member member, Member targetMember) {
        return blockRepository.save(Block.create(member, targetMember));
    }

}
