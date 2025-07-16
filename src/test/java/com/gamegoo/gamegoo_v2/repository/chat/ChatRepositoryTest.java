package com.gamegoo.gamegoo_v2.repository.chat;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.chat.domain.Chat;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.repository.ChatRepository;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    @Autowired
    private MemberRepository memberRepository;

    private static final int PAGE_SIZE = 20;

    private Member targetMember;
    private Member systemMember;
    private Chatroom chatroom;

    @BeforeEach
    void setUp() {
        targetMember = createMember("target@gmail.com", "targetMember");
        systemMember = createMember("system@gmail.com", "systemMember");
        chatroom = createChatroom();
    }

    @AfterEach
    void tearDown() {
        chatRepository.deleteAllInBatch();
        memberChatroomRepository.deleteAllInBatch();
        chatroomRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("최근 채팅 메시지 조회")
    class FindRecentChatsTest {

        @DisplayName("읽지 않은 메시지가 pageSize 보다 많은 경우, 읽지 않은 메시지만 정렬되어 반환한다.")
        @Test
        void findRecentChatsWithUnreadMessages() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastViewDate = now.minusMinutes(5);
            LocalDateTime lastJoinDate = now.minusMinutes(10);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);
            createMemberChatroom(targetMember, chatroom);

            for (int i = 1; i <= 30; i++) {
                createChat(member, "message " + i, chatroom);
            }

            // when
            Slice<Chat> chatSlice = chatRepository.findRecentChats(chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            List<Chat> chats = chatSlice.getContent();
            assertThat(chats).hasSize(30);
            assertThat(chatSlice.hasNext()).isFalse();
            assertThat(chats.get(0).getContents()).isEqualTo("message 1");
            assertThat(chats.get(29).getContents()).isEqualTo("message 30");
        }

        @DisplayName("읽지 않은 메시지가 pageSize 보다 적은 경우, 최근 메시지가 pageSize 만큼 정렬되어 반환한다.")
        @Test
        void findRecentChats() {
            // given
            LocalDateTime now = LocalDateTime.now();
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom);
            createMemberChatroom(targetMember, chatroom);

            // 채팅방 입장 처리
            memberChatroom.updateLastJoinDate(now);

            // 읽은 메시지 20개 생성
            for (int i = 1; i <= 20; i++) {
                createChatWithCreatedAt(member, "message " + i, chatroom, now.plusMinutes(5).plusSeconds(i));
            }

            // 채팅 메시지 읽음 처리
            memberChatroom.updateLastViewDate(now.plusMinutes(10));
            memberChatroomRepository.save(memberChatroom);

            // 안읽은 메시지 5개 생성
            for (int i = 1; i <= 5; i++) {
                Chat chat = createChat(member, "message " + (20 + i), chatroom);
                setChatCreatedAt(chat, now.plusMinutes(15).plusSeconds(i));
            }

            // when
            Slice<Chat> chatSlice = chatRepository.findRecentChats(chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            List<Chat> chats = chatSlice.getContent();
            assertThat(chatSlice).hasSize(PAGE_SIZE);
            assertThat(chatSlice.hasNext()).isTrue();
            assertThat(chats.get(0).getContents()).isEqualTo("message 6");
            assertThat(chats.get(19).getContents()).isEqualTo("message 25");
        }

        @DisplayName("lastViewDate 이후에 생성된 메시지만 정렬되어 반환된다.")
        @Test
        void findRecentChatsAfterLastViewDate() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastViewDate = now.minusMinutes(10);
            LocalDateTime lastJoinDate = now.minusMinutes(15);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);
            createMemberChatroom(targetMember, chatroom);

            // 20분 전 메시지 생성
            createChatWithCreatedAt(member, "old message", chatroom, now.minusMinutes(20));

            // 5분 전 메시지 생성
            createChatWithCreatedAt(member, "new message 1", chatroom, now.minusMinutes(5));
            createChatWithCreatedAt(member, "new message 2", chatroom, now.minusMinutes(5).plusSeconds(1));
            createChatWithCreatedAt(member, "new message 3", chatroom, now.minusMinutes(5).plusSeconds(2));

            // when
            Slice<Chat> chatSlice = chatRepository.findRecentChats(chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            List<Chat> chats = chatSlice.getContent();
            assertThat(chatSlice).hasSize(3);
            assertThat(chatSlice.hasNext()).isFalse();
            assertThat(chats.get(0).getContents()).isEqualTo("new message 1");
            assertThat(chats.get(2).getContents()).isEqualTo("new message 3");
        }

        @DisplayName("lastJoinDate 이후에 생성된 메시지만 정렬되어 반환된다.")
        @Test
        void findRecentChatsAfterLastJoinDate() {
            // given
            LocalDateTime lastViewDate = LocalDateTime.now();
            LocalDateTime lastJoinDate = LocalDateTime.now();
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);

            createMemberChatroom(targetMember, chatroom);

            // lastJoinDate 이전에 생성된 메시지
            createChatWithCreatedAt(member, "old message 1", chatroom, lastJoinDate.minusMinutes(2));
            createChatWithCreatedAt(member, "new message 2", chatroom, lastJoinDate.minusMinutes(1));

            // lastJoinDate 이후에 생성된 메시지
            createChatWithCreatedAt(member, "new message 1", chatroom, lastJoinDate.plusMinutes(1));
            createChatWithCreatedAt(member, "new message 2", chatroom, lastJoinDate.plusMinutes(2));
            createChatWithCreatedAt(member, "new message 3", chatroom, lastJoinDate.plusMinutes(3));

            // when
            Slice<Chat> chatSlice = chatRepository.findRecentChats(chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            List<Chat> chats = chatSlice.getContent();
            assertThat(chatSlice).hasSize(3);
            assertThat(chatSlice.hasNext()).isFalse();
            assertThat(chats.get(0).getContents()).isEqualTo("new message 1");
            assertThat(chats.get(2).getContents()).isEqualTo("new message 3");
        }

        @DisplayName("회원 메시지 또는 나에게 보낸 시스템 메시지만 정렬되어 반환된다.")
        @Test
        void findRecentChatsWithSystemAndUserMessages() {
            // given
            LocalDateTime lastViewDate = LocalDateTime.now().minusMinutes(10);
            LocalDateTime lastJoinDate = LocalDateTime.now().minusMinutes(10);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);

            createMemberChatroom(targetMember, chatroom);

            // 상대 회원에게 시스템 메시지 생성
            createSystemChat(targetMember, chatroom, 0);

            // 나에게 시스템 메시지 생성
            Chat memberSystemChat = createSystemChat(member, chatroom, 0);

            // when
            Slice<Chat> chatSlice = chatRepository.findRecentChats(chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            List<Chat> chats = chatSlice.getContent();
            assertThat(chatSlice).hasSize(1);
            assertThat(chatSlice.hasNext()).isFalse();
            assertThat(chats.get(0).getId()).isEqualTo(memberSystemChat.getId());
        }

        @DisplayName("메시지가 존재하지 않는 경우 빈 Slice를 반환한다.")
        @Test
        void findRecentChatsWhenNoMessages() {
            // given
            LocalDateTime lastViewDate = LocalDateTime.now();
            LocalDateTime lastJoinDate = LocalDateTime.now();
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);

            createMemberChatroom(targetMember, chatroom);

            // when
            Slice<Chat> chatSlice = chatRepository.findRecentChats(chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            assertThat(chatSlice).isEmpty();
            assertThat(chatSlice.hasNext()).isFalse();
        }

    }

    @Nested
    @DisplayName("cursor 기반 채팅 메시지 조회")
    class FindChatsByCursor {

        @DisplayName("cursor가 null인 경우, 가장 최근 메시지를 pageSize 만큼 조회 및 정렬되어 반환된다.")
        @Test
        void findChatsByCursorWhenCursorIsNull() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastViewDate = now.minusMinutes(5);
            LocalDateTime lastJoinDate = now.minusMinutes(10);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);
            createMemberChatroom(targetMember, chatroom);

            for (int i = 1; i <= 30; i++) {
                createChat(member, "message " + i, chatroom);
            }

            // when
            Slice<Chat> chatSlice = chatRepository.findChatsByCursor(null, chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            List<Chat> chats = chatSlice.getContent();
            assertThat(chats).hasSize(20);
            assertThat(chatSlice.hasNext()).isTrue();
            assertThat(chats.get(0).getContents()).isEqualTo("message 11");
            assertThat(chats.get(19).getContents()).isEqualTo("message 30");
        }

        @DisplayName("cursor를 정상 입력한 경우 cursor보다 이전에 생성된 메시지를 pageSize 만큼 조회 및 정렬되어 반환된다.")
        @Test
        void findChatsByCursorWithCursor() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastViewDate = now.minusMinutes(5);
            LocalDateTime lastJoinDate = now.minusMinutes(10);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);
            createMemberChatroom(targetMember, chatroom);

            long baseTimestamp = Instant.now().toEpochMilli();
            List<Chat> chatList = new ArrayList<>();

            for (int i = 1; i <= 30; i++) {
                chatList.add(createChatWithTimestamp(member, "message " + i, chatroom, baseTimestamp + i * 1000));
            }

            Long cursor = chatList.get(20).getTimestamp();

            // when
            Slice<Chat> chatSlice = chatRepository.findChatsByCursor(cursor, chatroom.getId(), member.getId(),
                    PAGE_SIZE);

            // then
            List<Chat> chats = chatSlice.getContent();
            assertThat(chats).hasSize(20);
            assertThat(chatSlice.hasNext()).isFalse();
            assertThat(chats.get(0).getContents()).isEqualTo("message 1");
            assertThat(chats.get(19).getContents()).isEqualTo("message 20");
        }

        @DisplayName("메시지가 없는 경우 빈 slice가 반환된다.")
        @Test
        void findChatsWhenNoMessages() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastViewDate = now.minusMinutes(5);
            LocalDateTime lastJoinDate = now.minusMinutes(10);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, lastViewDate, lastJoinDate);
            createMemberChatroom(targetMember, chatroom);

            // when
            Slice<Chat> chatSlice = chatRepository.findChatsByCursor(null, chatroom.getId(), member.getId(), PAGE_SIZE);

            // then
            assertThat(chatSlice).isEmpty();
            assertThat(chatSlice.hasNext()).isFalse();
        }

    }

    @Nested
    @DisplayName("안읽은 메시지 개수 조회")
    class CountUnreadChatsTest {

        @DisplayName("안읽은 메시지가 없는 경우 0을 반환한다.")
        @Test
        void countUnreadChatsSucceedsWhenNoUnreadChat() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastJoinDate = now.minusDays(1);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, now, lastJoinDate);
            createMemberChatroom(targetMember, chatroom);

            // 읽은 메시지 생성
            createChatWithCreatedAt(targetMember, "old message", chatroom, now.minusMinutes(20));
            createChatWithCreatedAt(targetMember, "old message", chatroom, now.minusMinutes(20).plusSeconds(1));
            createChatWithCreatedAt(targetMember, "old message", chatroom, now.minusMinutes(20).plusSeconds(2));

            // when
            int result = chatRepository.countUnreadChats(chatroom.getId(), member.getId());

            // then
            assertThat(result).isEqualTo(0);
        }

        @DisplayName("안읽은 메시지가 있는 경우 개수를 반환한다.")
        @Test
        void countUnreadChatsSucceeds() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastJoinDate = now.minusDays(1);
            MemberChatroom memberChatroom = createMemberChatroom(member, chatroom, now, lastJoinDate);
            createMemberChatroom(targetMember, chatroom);

            // 안읽은 메시지 생성
            createChatWithCreatedAt(targetMember, "old message", chatroom, now.plusMinutes(20));
            createChatWithCreatedAt(targetMember, "old message", chatroom, now.plusMinutes(20).plusSeconds(1));
            createChatWithCreatedAt(targetMember, "old message", chatroom, now.plusMinutes(20).plusSeconds(2));

            // when
            int result = chatRepository.countUnreadChats(chatroom.getId(), member.getId());

            // then
            assertThat(result).isEqualTo(3);
        }

    }

    private Chatroom createChatroom() {
        return em.persist(Chatroom.builder()
                .uuid(UUID.randomUUID().toString())
                .build());
    }

    private MemberChatroom createMemberChatroom(Member member, Chatroom chatroom) {
        return createMemberChatroom(member, chatroom, null, null);
    }

    private MemberChatroom createMemberChatroom(Member member, Chatroom chatroom, LocalDateTime lastViewDate,
                                                LocalDateTime lastJoinDate) {
        return em.persist(MemberChatroom.builder()
                .chatroom(chatroom)
                .member(member)
                .lastViewDate(lastViewDate)
                .lastJoinDate(lastJoinDate)
                .build());
    }

    private Chat createChat(Member fromMember, String contents, Chatroom chatroom) {
        return em.persist(Chat.builder()
                .contents(contents)
                .systemType(null)
                .chatroom(chatroom)
                .fromMember(fromMember)
                .toMember(null)
                .sourceBoard(null)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    private Chat createChatWithTimestamp(Member fromMember, String contents, Chatroom chatroom, Long timestamp) {
        return em.persist(Chat.builder()
                .contents(contents)
                .systemType(null)
                .chatroom(chatroom)
                .fromMember(fromMember)
                .toMember(null)
                .sourceBoard(null)
                .timestamp(timestamp)
                .build());
    }

    private Chat createChatWithCreatedAt(Member fromMember, String contents, Chatroom chatroom,
                                         LocalDateTime createdAt) {
        Chat chat = createChat(fromMember, contents, chatroom);
        setChatCreatedAt(chat, createdAt);
        return chat;
    }

    private Chat createSystemChat(Member toMember, Chatroom chatroom, Integer systemType) {
        return em.persist(Chat.builder()
                .contents("SYSTEM_MESSAGE")
                .systemType(systemType)
                .chatroom(chatroom)
                .fromMember(systemMember)
                .toMember(toMember)
                .sourceBoard(null)
                .build());
    }

    private void setChatCreatedAt(Chat chat, LocalDateTime createdAt) {
        EntityManager entityManager = em.getEntityManager();
        entityManager.createQuery("UPDATE Chat c SET c.createdAt = :createdAt WHERE c.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", chat.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

}
