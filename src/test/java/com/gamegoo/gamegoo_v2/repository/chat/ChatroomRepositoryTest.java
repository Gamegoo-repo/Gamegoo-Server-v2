package com.gamegoo.gamegoo_v2.repository.chat;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatroomRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member targetMember;

    @BeforeEach
    void setUp() {
        targetMember = createMember("target@gmail.com", "targetMember");
    }

    @AfterEach
    void tearDown() {
        memberChatroomRepository.deleteAllInBatch();
        chatroomRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("두 회원 간 채팅방 조회")
    class FindChatroomByMemberIdsTest {

        @DisplayName("채팅방이 존재하는 경우 해당 채팅방을 반환한다.")
        @Test
        void findChatroomByMemberIdsSucceeds() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            Optional<Chatroom> result = chatroomRepository.findChatroomByMemberIds(member.getId(),
                    targetMember.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(chatroom);
        }

        @DisplayName("채팅방이 존재하지 않는 경우 Optional.empty를 반환한다.")
        @Test
        void findChatroomByMemberIdsReturnsEmpty() {
            // when
            Optional<Chatroom> result = chatroomRepository.findChatroomByMemberIds(member.getId(),
                    targetMember.getId());

            // then
            assertThat(result).isEmpty();
        }

    }

    @Nested
    @DisplayName("입장한 채팅방 목록 조회")
    class FindActiveChatroomsTest {

        @DisplayName("입장한 채팅방이 존재하지 않는 경우 빈 list를 반환한다.")
        @Test
        void findActiveChatroomsSucceedsWithEmptyList() {
            //given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, null);

            // when
            List<Chatroom> result = chatroomRepository.findActiveChatrooms(member.getId());

            // then
            assertThat(result).isEmpty();
        }

        @DisplayName("입장한 채팅방이 존재하는 경우 해당 채팅방이 포함된 list를 반환한다.")
        @Test
        void findActiveChatroomsSucceeds() {
            // given
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom, LocalDateTime.now());
            createMemberChatroom(targetMember, chatroom, null);

            // when
            List<Chatroom> result = chatroomRepository.findActiveChatrooms(member.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(chatroom.getId());
        }

    }

    private Chatroom createChatroom() {
        return em.persist(Chatroom.builder()
                .uuid(UUID.randomUUID().toString())
                .build());
    }

    private MemberChatroom createMemberChatroom(Member member, Chatroom chatroom, LocalDateTime lastJoinDate) {
        return em.persist(MemberChatroom.builder()
                .chatroom(chatroom)
                .member(member)
                .lastViewDate(null)
                .lastJoinDate(lastJoinDate)
                .build());
    }

}
