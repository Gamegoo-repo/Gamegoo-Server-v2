package com.gamegoo.gamegoo_v2.service.scheduler;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.chat.domain.Chatroom;
import com.gamegoo.gamegoo_v2.chat.domain.MemberChatroom;
import com.gamegoo.gamegoo_v2.chat.repository.ChatRepository;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.core.scheduler.handler.MannerMessageHandler;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MannerMessageStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
public class MannerMessageHandlerTest {

    private Member member;
    private Member targetMember;

    @Autowired
    private MannerMessageHandler mannerMessageHandler;

    @Autowired
    private MatchingRecordRepository matchingRecordRepository;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    @Autowired
    private ChatRepository chatRepository;

    @MockitoBean
    private SocketService socketService;

    @MockitoSpyBean
    private MemberRepository memberRepository;

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
        matchingRecordRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("매너평가 메시지 전송 프로세스")
    @Test
    void mannerMessageHandlerProcess() {
        // given
        // 매칭 기록 저장
        MatchingRecord matchingRecord = createMatchingRecord(member, GameMode.FAST);
        MatchingRecord targetMatchingRecord = createMatchingRecord(targetMember, GameMode.FAST);

        matchingRecord.updateTargetMatchingRecord(targetMatchingRecord);
        targetMatchingRecord.updateTargetMatchingRecord(matchingRecord);
        matchingRecordRepository.save(matchingRecord);
        matchingRecordRepository.save(targetMatchingRecord);

        // 채팅방 생성
        Chatroom chatroom = createChatroom();
        createMemberChatroom(member, chatroom, null);
        createMemberChatroom(targetMember, chatroom, null);

        // 시스템 회원 mock 처리
        Member systemMember = createMember("sytemMember@gmail.com", "systemMember");
        given(memberRepository.findById(0L)).willReturn(Optional.of(systemMember));

        // when
        mannerMessageHandler.process(matchingRecord);

        // then
        // matchingRecord의 매너 평가 메시지 전송 여부 업데이트 되었는지 확인
        assertThat(matchingRecord.getMannerMessageSent()).isEqualTo(MannerMessageStatus.SENT);

        // 시스템 메시지 저장 되었는지 확인
        assertThat(chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(), systemMember.getId())).hasSize(1);

        // socket API 호출 되었는지 확인
        verify(socketService).sendSystemMessage(any(Long.class), any(String.class), any(String.class), any(Long.class));
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

    private MatchingRecord createMatchingRecord(Member member, GameMode gameMode) {
        return matchingRecordRepository.save(MatchingRecord.builder()
                .gameMode(gameMode)
                .mainP(member.getMainP())
                .subP(member.getSubP())
                .wantP(member.getWantP().isEmpty() ? null : member.getWantP().get(0))
                .mike(member.getMike())
                .tier(member.getSoloTier())
                .gameRank(member.getSoloRank())
                .winrate(member.getSoloWinRate())
                .matchingType(MatchingType.BASIC)
                .mannerLevel(member.getMannerLevel())
                .member(member)
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

}
