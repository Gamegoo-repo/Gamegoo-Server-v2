package com.gamegoo.gamegoo_v2.integration.matching;

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
import com.gamegoo.gamegoo_v2.chat.repository.ChatRepository;
import com.gamegoo.gamegoo_v2.chat.repository.ChatroomRepository;
import com.gamegoo.gamegoo_v2.chat.repository.MemberChatroomRepository;
import com.gamegoo.gamegoo_v2.core.exception.ChatException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import com.gamegoo.gamegoo_v2.matching.dto.request.InitializingMatchingRequest;
import com.gamegoo.gamegoo_v2.matching.dto.response.MatchingMemberInfoResponse;
import com.gamegoo.gamegoo_v2.matching.dto.response.PriorityListResponse;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import com.gamegoo.gamegoo_v2.matching.service.MatchingFacadeService;
import com.gamegoo.gamegoo_v2.matching.service.MatchingService;
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
import org.w3c.dom.ls.LSInput;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.as;
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

    @Autowired
    MatchingService matchingService;

    @Autowired
    private MatchingRecordRepository matchingRecordRepository;

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
        matchingRecordRepository.deleteAllInBatch();
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

    @DisplayName("매칭 우선순위 계산 및 DB 저장 테스트")
    @Test
    void getPriorityListAndCheckRecord() {
        // given
        // 유저 정보 생성
        Member matchingMember = createMatchingMember("user1@gmail.com", "User1", "Tag1", Tier.GOLD, 2, true,
                Position.ADC, Position.MID, Position.SUP, 2);

        // dto 생성
        InitializingMatchingRequest request = InitializingMatchingRequest.builder()
                .mike(Mike.UNAVAILABLE)
                .matchingType(MatchingType.BASIC)
                .mainP(Position.TOP)
                .subP(Position.ADC)
                .wantP(Position.JUNGLE)
                .gameMode(GameMode.SOLO)
                .gameStyleIdList(List.of())
                .build();

        MatchingRecord matchingRecord = MatchingRecord.create(request.getGameMode(), request.getMatchingType(),
                matchingMember);
        matchingRecord.updateStatus(MatchingStatus.PENDING);

        // 랜덤 대기 유저 생성
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            // 랜덤값 생성
            String email = "user" + i + "@gmail.com";
            String gameName = "USER" + i;
            String tag = "TAG" + i;
            Tier tier = Tier.values()[random.nextInt(Tier.values().length)];
            int gameRank = random.nextInt(4) + 1;
            boolean hasMike = random.nextBoolean();
            Position mainP = Position.values()[random.nextInt(Position.values().length)];
            Position subP = Position.values()[random.nextInt(Position.values().length)];
            Position wantP = Position.values()[random.nextInt(Position.values().length)];
            int mannerLevel = random.nextInt(4) + 1;
            GameMode randomGameMode = GameMode.values()[random.nextInt(GameMode.values().length)];
            MatchingType randomMatchingType = MatchingType.values()[random.nextInt(MatchingType.values().length)];
            MatchingStatus randomMatchingStatus = MatchingStatus.PENDING;

            Member targetMember = createMatchingMember(email, gameName, tag, tier, gameRank, hasMike, mainP, subP,
                    wantP, mannerLevel);
            createMatchingRecord(randomGameMode, randomMatchingType, targetMember, randomMatchingStatus);
        }

        // when
        PriorityListResponse priorityListResponse =
                matchingFacadeService.calculatePriorityAndRecording(matchingMember.getId(), request);

        // then
        assertThat(priorityListResponse).isNotNull();

        // 1. Member 정보 업데이트 검증
        //assertThat(matchingMember.getMike()).isEqualTo(request.getMike());
        //assertThat(matchingMember.getMainPosition()).isEqualTo(request.getMainP());
        //assertThat(matchingMember.getSubPosition()).isEqualTo(request.getSubP());
        //assertThat(matchingMember.getWantPosition()).isEqualTo(request.getWantP());
        //assertThat(matchingMember.getMemberGameStyleList()).isEqualTo(request.getGameStyleIdList());

        // 2. 생성된 MatchingRecord 검증

        // 3. Priority 검증
        List<MatchingRecord> recentValidMatchingRecords =
                matchingRecordRepository.findRecentValidMatchingRecords(request.getGameMode());
        PriorityListResponse expectedPriorityList = matchingService.calculatePriorityList(matchingRecord,
                recentValidMatchingRecords);

        assertThat(priorityListResponse).isNotNull();

        // 정렬 (ID 기준으로 정렬하여 비교)
        Comparator<PriorityValue> priorityComparator = Comparator.comparing(PriorityValue::getMemberId);
        expectedPriorityList.getMyPriorityList().sort(priorityComparator);
        expectedPriorityList.getOtherPriorityList().sort(priorityComparator);
        priorityListResponse.getMyPriorityList().sort(priorityComparator);
        priorityListResponse.getOtherPriorityList().sort(priorityComparator);

        assertThat(priorityListResponse.getMyPriorityList()).isEqualTo(expectedPriorityList.getMyPriorityList());
        assertThat(priorityListResponse.getOtherPriorityList()).isEqualTo(expectedPriorityList.getOtherPriorityList());
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

    private Member createMatchingMember(String email, String gameName, String tag, Tier tier, int gameRank,
                                        boolean hasMike,
                                        Position mainP, Position subP,
                                        Position wantP, int mannerLevel) {

        Member member = Member.create(email, "password123", LoginType.GENERAL, gameName, tag, tier, gameRank, 55.0,
                100, hasMike);
        member.updateMannerLevel(mannerLevel);
        member.updatePosition(mainP, subP, wantP);
        return memberRepository.save(member);
    }

    private MatchingRecord createMatchingRecord(GameMode mode, MatchingType type, Member member,
                                                MatchingStatus status) {
        MatchingRecord matchingRecord = MatchingRecord.create(mode, type, member);
        matchingRecord.updateStatus(status);
        return matchingRecordRepository.save(matchingRecord);
    }

}
