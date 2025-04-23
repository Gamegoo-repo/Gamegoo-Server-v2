package com.gamegoo.gamegoo_v2.repository.chat;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberChatroomRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        memberChatroomRepository.deleteAllInBatch();
        chatroomRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("채팅 상대 회원 배치 조회")
    @Test
    void findTargetMembersBatch() {
        // given
        List<Long> chatroomIds = new ArrayList<>();
        List<Member> targetMembers = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Member targetMember = createMember("targetMember" + i + "@gmail.com", "targetMember" + i);
            Chatroom chatroom = createForGeneralChatroom();
            createForGeneralMemberChatroom(member, chatroom);
            createForGeneralMemberChatroom(targetMember, chatroom);
            chatroomIds.add(chatroom.getId());
            targetMembers.add(targetMember);
        }

        // when
        Map<Long, Member> targetMemberMap = memberChatroomRepository.findTargetMembersBatch(chatroomIds,
                member.getId());

        // then
        assertThat(targetMemberMap).hasSize(chatroomIds.size());
        for (Member targetMember : targetMembers) {
            assertThat(targetMember).isIn(targetMemberMap.values());
        }
    }

    @DisplayName("채팅방 lastJoinDate 배치 초기화")
    @Test
    void updateLastJoinDateToNullByMember() {
        // given
        for (int i = 0; i < 3; i++) {
            Member targetMember = createMember("targetMember" + i + "@gmail.com", "targetMember" + i);
            Chatroom chatroom = createChatroom();
            createMemberChatroom(member, chatroom);
            createMemberChatroom(targetMember, chatroom);
        }

        // when
        memberChatroomRepository.updateLastJoinDateToNullByMember(member);

        // then
        List<MemberChatroom> updatedList = memberChatroomRepository.findAllByMemberId(member.getId());
        assertThat(updatedList).hasSize(3);
        assertThat(updatedList).allSatisfy(mc -> assertThat(mc.getLastJoinDate()).isNull());
    }

    private Chatroom createForGeneralChatroom() {
        return em.persist(Chatroom.builder()
                .uuid(UUID.randomUUID().toString())
                .build());
    }

    private MemberChatroom createForGeneralMemberChatroom(Member member, Chatroom chatroom) {
        return em.persist(MemberChatroom.builder()
                .chatroom(chatroom)
                .member(member)
                .lastViewDate(null)
                .lastJoinDate(null)
                .build());
    }

}
