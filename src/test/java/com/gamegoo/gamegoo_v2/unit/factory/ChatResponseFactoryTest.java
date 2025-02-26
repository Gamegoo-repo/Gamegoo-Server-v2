package com.gamegoo.gamegoo_v2.unit.factory;

import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.dto.ChatResponseFactory;
import com.gamegoo.gamegoo_v2.chat.dto.response.ChatMessageListResponse;
import com.gamegoo.gamegoo_v2.chat.dto.response.EnterChatroomResponse;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.utils.TimestampUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatResponseFactoryTest {

    @Mock
    private FriendService friendService;

    @Mock
    private BlockService blockService;

    @InjectMocks
    private ChatResponseFactory chatResponseFactory;

    private Chatroom chatroom = Chatroom.create("uuid");
    private Chat chat1;
    private Chat chat2;
    private Chat systemChat;

    private Member member;
    private Member targetMember;
    private Member systemMember;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
        targetMember = createMember("target@gmail.com", "targetMember");
        systemMember = createMember("system@gmail.com", "systemMember");

        chat1 = createChat(member, "Hello", chatroom);
        chat2 = createChat(targetMember, "Hi", chatroom);
        systemChat = createSystemChat(member, chatroom, 1);
    }


    @Nested
    @DisplayName("toChatMessageListResponse(Slice<Chat>)")
    class ToChatMessageListResponseTest {

        @Test
        @DisplayName("성공: 빈 Slice일 경우")
        void toChatMessageListResponse_WhenSliceIsEmpty() {
            // given
            Slice<Chat> emptySlice = new SliceImpl<>(List.of());

            // when
            ChatMessageListResponse response = chatResponseFactory.toChatMessageListResponse(emptySlice);

            // then
            assertThat(response.getChatMessageList()).isEmpty();
            assertThat(response.getListSize()).isEqualTo(0);
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }

        @Test
        @DisplayName("성공: 일반 채팅 메시지가 있는 경우")
        void toChatMessageListResponse_WithRegularChats() {
            // given
            Slice<Chat> chatSlice = new SliceImpl<>(List.of(chat1, chat2));

            // when
            ChatMessageListResponse response = chatResponseFactory.toChatMessageListResponse(chatSlice);

            // then
            assertThat(response.getChatMessageList()).hasSize(2);
            assertThat(response.getListSize()).isEqualTo(2);
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }

        @Test
        @DisplayName("성공: 시스템 메시지가 포함된 경우")
        void toChatMessageListResponse_WithSystemMessages() {
            // given
            Slice<Chat> chatSlice = new SliceImpl<>(List.of(chat1, systemChat));

            // when
            ChatMessageListResponse response = chatResponseFactory.toChatMessageListResponse(chatSlice);

            // then
            assertThat(response.getChatMessageList()).hasSize(2);
            assertThat(response.getListSize()).isEqualTo(2);
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }

        @Test
        @DisplayName("성공: 다음 페이지가 있는 경우")
        void toChatMessageListResponse_WhenHasNextIsTrue() {
            // given
            Slice<Chat> chatSlice = new SliceImpl<>(List.of(chat1, chat2), Pageable.unpaged(), true);

            // when
            ChatMessageListResponse response = chatResponseFactory.toChatMessageListResponse(chatSlice);

            // then
            assertThat(response.getChatMessageList()).hasSize(2);
            assertThat(response.getListSize()).isEqualTo(2);
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getNextCursor()).isEqualTo(chat1.getTimestamp());
        }

    }

    @Nested
    @DisplayName("toChatMessageListResponse()")
    class ToChatMessageListResponseEmptyTest {

        @Test
        @DisplayName("성공: 빈 메시지 리스트를 반환한다")
        void toChatMessageListResponse_ReturnsEmptyResponse() {
            // when
            ChatMessageListResponse response = chatResponseFactory.toChatMessageListResponse();

            // then
            assertThat(response.getChatMessageList()).isEmpty();
            assertThat(response.getListSize()).isEqualTo(0);
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }

    }

    @Nested
    @DisplayName("toEnterChatroomResponse()")
    class ToEnterChatroomResponseTest {

        private String chatroomUuid = UUID.randomUUID().toString();
        private ChatMessageListResponse chatMessageListResponse;

        @BeforeEach
        void setUp() {
            chatMessageListResponse = ChatMessageListResponse.builder()
                    .chatMessageList(new ArrayList<>())
                    .listSize(0)
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        @Test
        @DisplayName("성공: 정상적으로 EnterChatroomResponse를 생성한다")
        void toEnterChatroomResponse_Success() {
            // given
            when(friendService.isFriend(member, targetMember)).thenReturn(true);
            when(blockService.isBlocked(member, targetMember)).thenReturn(false);
            when(friendService.getFriendRequestMemberId(member, targetMember)).thenReturn(null);

            // when
            EnterChatroomResponse response = chatResponseFactory.toEnterChatroomResponse(member, targetMember,
                    chatroomUuid, chatMessageListResponse);

            // then
            assertThat(response.getUuid()).isEqualTo(chatroomUuid);
            assertThat(response.getMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getGameName()).isEqualTo(targetMember.getGameName());
            assertThat(response.getMemberProfileImg()).isEqualTo(targetMember.getProfileImage());
            assertThat(response.isFriend()).isTrue();
            assertThat(response.isBlocked()).isFalse();
            assertThat(response.isBlind()).isFalse();
            assertThat(response.getFriendRequestMemberId()).isNull();
            assertThat(response.getChatMessageListResponse()).isEqualTo(chatMessageListResponse);
        }

        @Test
        @DisplayName("성공: 상대가 탈퇴한 경우 게임 이름을 '(탈퇴한 사용자)'로 반환한다")
        void toEnterChatroomResponse_WhenTargetMemberIsBlind() {
            // given
            targetMember.updateBlind(true);

            when(friendService.isFriend(member, targetMember)).thenReturn(false);
            when(blockService.isBlocked(member, targetMember)).thenReturn(false);
            when(friendService.getFriendRequestMemberId(member, targetMember)).thenReturn(null);

            // when
            EnterChatroomResponse response = chatResponseFactory.toEnterChatroomResponse(member, targetMember,
                    chatroomUuid, chatMessageListResponse);

            // then
            assertThat(response.getGameName()).isEqualTo("(탈퇴한 사용자)");
            assertThat(response.isBlind()).isTrue();
        }

    }

    private Member createMember(String email, String gameName) {
        return Member.builder()
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
                .isAgree(true)
                .build();
    }

    private Chat createChat(Member fromMember, String contents, Chatroom chatroom) {
        return Chat.builder()
                .contents(contents)
                .timestamp(TimestampUtil.getNowUtcTimeStamp())
                .systemType(null)
                .chatroom(chatroom)
                .fromMember(fromMember)
                .toMember(null)
                .sourceBoard(null)
                .build()
                .withCreatedAt(LocalDateTime.now());
    }

    private Chat createSystemChat(Member toMember, Chatroom chatroom, Integer systemType) {
        return Chat.builder()
                .contents("SYSTEM_MESSAGE")
                .timestamp(TimestampUtil.getNowUtcTimeStamp())
                .systemType(systemType)
                .chatroom(chatroom)
                .fromMember(systemMember)
                .toMember(toMember)
                .sourceBoard(null)
                .build()
                .withCreatedAt(LocalDateTime.now());
    }

}
