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
import com.gamegoo.gamegoo_v2.core.exception.MatchingException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
import com.gamegoo.gamegoo_v2.external.socket.SocketService;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MannerMessageStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import com.gamegoo.gamegoo_v2.matching.dto.request.InitializingMatchingRequest;
import com.gamegoo.gamegoo_v2.matching.dto.response.MatchingFoundResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
@Transactional
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
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(socketService, times(1)).joinSocketToChatroom(eq(member.getId()), eq(chatroom.getUuid())));

            // targetMember의 lastJoinDate 검증
            MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    targetMember.getId(), chatroom.getId()).orElseThrow();
            assertThat(targetMemberChatroom.getLastJoinDate()).isCloseTo(lastJoinDate, within(1, ChronoUnit.SECONDS));

            // 매칭 시스템 메시지 생성 검증
            List<Chat> systemChats = chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(),
                    systemMember.getId());
            assertThat(systemChats).hasSize(2);
            systemChats.forEach(systemChat -> assertThat(systemChat.getContents()).isEqualTo(SystemMessageType.MATCH_SUCCESS_MESSAGE.getMessage()));
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
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(socketService, times(2)).joinSocketToChatroom(any(Long.class), eq(chatroom.getUuid())));

            // 매칭 시스템 메시지 생성 검증
            List<Chat> systemChats = chatRepository.findByChatroomIdAndFromMemberId(chatroom.getId(),
                    systemMember.getId());
            assertThat(systemChats).hasSize(2);
            systemChats.forEach(systemChat -> assertThat(systemChat.getContents()).isEqualTo(SystemMessageType.MATCH_SUCCESS_MESSAGE.getMessage()));
        }

    }

    @DisplayName("매칭 우선순위 계산 및 DB 저장 테스트")
    @Test
    void getPriorityListAndCheckRecord() {
        // given
        // 유저 정보 생성
        Member matchingMember = createMatchingMember("matchinguser@gmail.com", "User1", "Tag1", Tier.GOLD, 2,
                Mike.AVAILABLE, Position.ADC, Position.MID, List.of(Position.SUP), 2);

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

        // 랜덤 대기 유저 생성
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            // 랜덤값 생성
            String email = "user" + i + "@gmail.com";
            String gameName = "USER" + i;
            String tag = "TAG" + i;
            Tier tier = Tier.values()[random.nextInt(Tier.values().length)];
            int gameRank = random.nextInt(4) + 1;
            Mike mike = Mike.values()[random.nextInt(Mike.values().length)];
            Position mainP = Position.values()[random.nextInt(Position.values().length)];
            Position subP = Position.values()[random.nextInt(Position.values().length)];
            Position wantP = Position.values()[random.nextInt(Position.values().length)];
            int mannerLevel = random.nextInt(4) + 1;
            GameMode randomGameMode = GameMode.values()[random.nextInt(GameMode.values().length)];
            MatchingType randomMatchingType = MatchingType.values()[random.nextInt(MatchingType.values().length)];
            MatchingStatus randomMatchingStatus = MatchingStatus.PENDING;

            Member targetMember = createMatchingMember(email, gameName, tag, tier, gameRank, mike, mainP, subP,
                    List.of(wantP), mannerLevel);
            createMatchingRecord(randomGameMode, randomMatchingType, targetMember, randomMatchingStatus);
        }

        // when
        PriorityListResponse priorityListResponse =
                matchingFacadeService.calculatePriorityAndRecording(matchingMember.getId(), request);

        Member updatedMember = memberRepository.findByEmail("matchinguser@gmail.com")
                .orElseThrow(() -> new AssertionError("테스트 실패: Member가 존재하지 않음"));

        MatchingRecord matchingRecord = MatchingRecord.create(request.getGameMode(), request.getMatchingType(),
                updatedMember);
        matchingRecord.updateStatus(MatchingStatus.PENDING);

        // then
        assertThat(priorityListResponse).isNotNull();

        // 1. Member 정보 업데이트 검증
        assertThat(updatedMember.getMike()).isEqualTo(request.getMike());
        assertThat(updatedMember.getMainP()).isEqualTo(request.getMainP());
        assertThat(updatedMember.getSubP()).isEqualTo(request.getSubP());
        assertThat(updatedMember.getWantPositions().get(0)).isEqualTo(request.getWantP());

        // 2. 생성된 MatchingRecord 검증
        Optional<MatchingRecord> matchingRecordOptional = matchingRecordRepository.findLatestByMember(updatedMember);
        MatchingRecord actualMatchingRecord = matchingRecordOptional.orElseThrow();
        assertThat(actualMatchingRecord.getGameMode()).isEqualTo(request.getGameMode());
        assertThat(actualMatchingRecord.getMatchingType()).isEqualTo(request.getMatchingType());
        assertThat(actualMatchingRecord.getStatus()).isEqualTo(MatchingStatus.PENDING);
        assertThat(actualMatchingRecord.getMember().getId()).isEqualTo(updatedMember.getId());
        assertThat(actualMatchingRecord.getMainP()).isEqualTo(request.getMainP());
        assertThat(actualMatchingRecord.getSubP()).isEqualTo(request.getSubP());
        assertThat(actualMatchingRecord.getWantP()).isEqualTo(request.getWantP());
        assertThat(actualMatchingRecord.getMike()).isEqualTo(request.getMike());

        // 3. Priority 검증
        List<MatchingRecord> recentValidMatchingRecords =
                matchingRecordRepository.findValidMatchingRecords(LocalDateTime.now().minusMinutes(5), GameMode.SOLO,
                        member.getId());
        PriorityListResponse expectedPriorityList = matchingService.calculatePriorityList(matchingRecord,
                recentValidMatchingRecords);

        assertThat(priorityListResponse).isNotNull();

        // 정렬 (ID 기준으로 정렬하여 비교)
        Comparator<PriorityValue> priorityComparator = Comparator.comparing(PriorityValue::getMemberId);
        expectedPriorityList.getMyPriorityList().sort(priorityComparator);
        expectedPriorityList.getOtherPriorityList().sort(priorityComparator);
        priorityListResponse.getMyPriorityList().sort(priorityComparator);
        priorityListResponse.getOtherPriorityList().sort(priorityComparator);

        assertThat(priorityListResponse.getMyPriorityList())
                .usingRecursiveComparison()
                .ignoringFields("matchingUuid")
                .isEqualTo(expectedPriorityList.getMyPriorityList());
        assertThat(priorityListResponse.getOtherPriorityList())
                .usingRecursiveComparison()
                .ignoringFields("matchingUuid")
                .isEqualTo(expectedPriorityList.getOtherPriorityList());
    }

    @DisplayName("내 매칭 status 변경")
    @Nested
    class changeMyStatus {

        @DisplayName("성공: 매칭 status 변경된 경우")
        @Test
        void updateMatchingStatusSucceed() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);

            // when
            matchingFacadeService.modifyMyMatchingStatus(matchingRecord.getMatchingUuid(), MatchingStatus.FAIL);

            // then
            assertThat(matchingRecord.getStatus()).isEqualTo(MatchingStatus.FAIL);

        }

        @DisplayName("실패: 매칭 uuid가 잘못된 경우")
        @Test
        void updateMatchingStatusFail() {
            // given
            createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member, MatchingStatus.PENDING);

            // when
            assertThatThrownBy(() -> matchingFacadeService.modifyMyMatchingStatus("wrongUuid", MatchingStatus.FAIL))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_NOT_FOUND.getMessage());
        }


    }

    @DisplayName("상대방과 내 매칭 status 변경")
    @Nested
    class changeBothStatus {

        @DisplayName("성공: 매칭 status 변경된 경우")
        @Test
        void updateMatchingStatusSucceed() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);
            matchingRecord.updateTargetMatchingRecord(targetMatchingRecord);
            targetMatchingRecord.updateTargetMatchingRecord(matchingRecord);

            // when
            matchingFacadeService.modifyBothMatchingStatus(matchingRecord.getMatchingUuid(), MatchingStatus.FAIL);

            // then
            assertThat(matchingRecord.getStatus()).isEqualTo(MatchingStatus.FAIL);
            assertThat(targetMatchingRecord.getStatus()).isEqualTo(MatchingStatus.FAIL);

        }

        @DisplayName("실패: 매칭 상대가 없을 경우")
        @Test
        void updateMatchingStatusFail() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);

            // when
            assertThatThrownBy(() -> matchingFacadeService.modifyBothMatchingStatus(matchingRecord.getMatchingUuid(),
                    MatchingStatus.FAIL))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.TARGET_MATCHING_MEMBER_NOT_FOUND.getMessage());
        }


    }

    @DisplayName("매칭 Found : targetMember 지정 및 status 변경")
    @Nested
    class MatchingFound {

        @DisplayName("실패: 두 회원이 동일한 경우 예외가 발생한다.")
        @Test
        void matchingFoundFailWhenMemberDuplicate() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    member, MatchingStatus.PENDING);


            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(GlobalException.class);
        }

        @DisplayName("실패: 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void matchingFoundFailWhenMemberIsBlind() {
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // given
            blindMember(targetMember);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 상대 회원을 차단한 경우 예외가 발생한다.")
        @Test
        void matchingFoundFailWhenTargetMemberBlocked() {
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // given
            blockMember(member, targetMember);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.MATCHING_FOUND_FAILED_TARGET_IS_BLOCKED.getMessage());
        }

        @DisplayName("실패: 상대 회원에게 차단 당한 경우 예외가 발생한다.")
        @Test
        void matchingFoundFailWhenBlockedByTargetMember() {
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // given
            blockMember(targetMember, member);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.MATCHING_FOUND_FAILED_BLOCKED_BY_TARGET.getMessage());
        }

        @DisplayName("실패: 매칭 uuid에 해당하는 매칭 기록이 없는 경우")
        @Test
        void notFoundMatchingUuid() {
            // given
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when, then
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(UUID.randomUUID().toString(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 매칭 uuid에 해당하는 상대 매칭 기록이 없는 경우")
        @Test
        void notFoundTargetMatchingUuid() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    member, MatchingStatus.PENDING);

            // when, then
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                    UUID.randomUUID().toString()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 내 매칭 status가 Pending이 아니었던 경우")
        @Test
        void myMatchingStatusNotPending() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.FAIL);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when then
            assertThat(matchingRecord.getTargetMatchingRecord()).isNull();
            assertThat(targetMatchingRecord.getTargetMatchingRecord()).isNull();
            assertThat(matchingRecord.getStatus()).isEqualTo(MatchingStatus.FAIL);
            assertThat(targetMatchingRecord.getStatus()).isEqualTo(MatchingStatus.PENDING);
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_STATUS_NOT_ALLOWED.getMessage());
        }

        @DisplayName("실패: 상대 매칭 status가 Pending이 아니었던 경우")
        @Test
        void targetMatchingStatusNotPending() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.SUCCESS);

            // when then
            assertThat(matchingRecord.getTargetMatchingRecord()).isNull();
            assertThat(targetMatchingRecord.getTargetMatchingRecord()).isNull();
            assertThat(matchingRecord.getStatus()).isEqualTo(MatchingStatus.PENDING);
            assertThat(targetMatchingRecord.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
            assertThatThrownBy(() -> matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_TARGET_UNAVAILABLE.getMessage());
        }

        @DisplayName("성공: matching status 변경 및 target 지정")
        @Test
        void matchingFoundSucceed() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when
            MatchingFoundResponse matchingFoundResponse =
                    matchingFacadeService.matchingFound(matchingRecord.getMatchingUuid(),
                            targetMatchingRecord.getMatchingUuid());

            // then
            assertThat(matchingRecord.getStatus()).isEqualTo(MatchingStatus.FOUND);
            assertThat(targetMatchingRecord.getStatus()).isEqualTo(MatchingStatus.FOUND);
            assertThat(targetMatchingRecord.getTargetMatchingRecord().getMatchingUuid()).isEqualTo(matchingRecord.getMatchingUuid());
            assertThat(matchingRecord.getTargetMatchingRecord().getMatchingUuid()).isEqualTo(targetMatchingRecord.getMatchingUuid());

            MatchingFoundResponse expectedResponse = MatchingFoundResponse.of(matchingRecord, targetMatchingRecord);
            assertThat(matchingFoundResponse).isEqualTo(expectedResponse);

        }

    }

    @DisplayName("매칭 Success : 매칭 status, 매너 매세지 전송 status 변경 및 채팅방 Uuid 전송")
    @Nested
    class MatchingSuccess {

        @DisplayName("실패: 두 회원이 동일한 경우 예외가 발생한다.")
        @Test
        void matchingSuccessFailWhenMemberDuplicate() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    member, MatchingStatus.PENDING);


            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(GlobalException.class);
        }

        @DisplayName("실패: 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void matchingSuccessFailWhenMemberIsBlind() {
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // given
            blindMember(targetMember);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 상대 회원을 차단한 경우 예외가 발생한다.")
        @Test
        void matchingSuccessFailWhenTargetMemberBlocked() {
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // given
            blockMember(member, targetMember);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.MATCHING_FOUND_FAILED_TARGET_IS_BLOCKED.getMessage());
        }

        @DisplayName("실패: 상대 회원에게 차단 당한 경우 예외가 발생한다.")
        @Test
        void matchingFoundSuccessWhenBlockedByTargetMember() {
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // given
            blockMember(targetMember, member);

            // when // then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(ChatException.class)
                    .hasMessage(ErrorCode.MATCHING_FOUND_FAILED_BLOCKED_BY_TARGET.getMessage());
        }

        @DisplayName("실패: 매칭 uuid에 해당하는 매칭 기록이 없는 경우")
        @Test
        void notSuccessMatchingUuid() {
            // given
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when, then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(UUID.randomUUID().toString(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 매칭 uuid에 해당하는 상대 매칭 기록이 없는 경우")
        @Test
        void notSuccessTargetMatchingUuid() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    member, MatchingStatus.PENDING);

            // when, then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    UUID.randomUUID().toString()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 내 매칭 status가 Found가 아니었던 경우")
        @Test
        void myMatchingStatusNotFound() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.FAIL);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.FOUND);

            // when then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_STATUS_NOT_ALLOWED.getMessage());
        }

        @DisplayName("실패: 상대 매칭 status가 Found 아니었던 경우")
        @Test
        void targetMatchingStatusNotPending() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.FOUND);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.SUCCESS);

            // when then
            assertThatThrownBy(() -> matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid()))
                    .isInstanceOf(MatchingException.class)
                    .hasMessage(ErrorCode.MATCHING_TARGET_UNAVAILABLE.getMessage());
        }

        @DisplayName("성공: 매칭 status, 매너 매세지 전송 status 변경 및 채팅방 Uuid 전송, 기존 채팅방이 존재하는 경우")
        @Test
        void matchingFoundSucceedWhenChatroomExist() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.FOUND);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.FOUND);

            // 기존 채팅방이 존재하는 경우를 검증하기 위해 채팅방을 생성
            Chatroom chatroom = createChatroom();
            LocalDateTime lastJoinDate = LocalDateTime.now().minusDays(1);
            createMemberChatroom(member, chatroom, null);
            createMemberChatroom(targetMember, chatroom, lastJoinDate);

            Member systemMember = createMember("systemMember@gmail.com", "systemMember");
            given(memberRepository.findById(0L)).willReturn(Optional.of(systemMember));

            // when
            String chatUuid = matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid());

            // then
            assertThat(matchingRecord.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
            assertThat(matchingRecord.getMannerMessageSent()).isEqualTo(MannerMessageStatus.NOT_SENT);
            assertThat(targetMatchingRecord.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
            assertThat(targetMatchingRecord.getMannerMessageSent()).isEqualTo(MannerMessageStatus.NOT_SENT);

            // 채팅방이 존재하는 경우 기존 채팅방이 사용되는지 검증
            Chatroom foundChatroom = chatroomRepository.findByUuid(chatUuid).orElseThrow();
            assertThat(foundChatroom.getUuid()).isEqualTo(chatroom.getUuid());

            // member의 lastJoinDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    foundChatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastJoinDate()).isNotNull();

            // targetMember의 lastJoinDate 검증
            MemberChatroom targetMemberChatroom =
                    memberChatroomRepository.findByMemberIdAndChatroomId(targetMember.getId(), foundChatroom.getId()).orElseThrow();
            assertThat(targetMemberChatroom.getLastJoinDate()).isCloseTo(lastJoinDate, within(1, ChronoUnit.SECONDS));

            // socket service 호출 여부 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(socketService, times(1)).joinSocketToChatroom(eq(member.getId()), eq(foundChatroom.getUuid())));

            // 매칭 시스템 메시지 생성 검증
            List<Chat> systemChats = chatRepository.findByChatroomIdAndFromMemberId(foundChatroom.getId(),
                    systemMember.getId());
            assertThat(systemChats).hasSize(2);
            systemChats.forEach(systemChat -> assertThat(systemChat.getContents()).isEqualTo(SystemMessageType.MATCH_SUCCESS_MESSAGE.getMessage()));

        }

        @DisplayName("성공: 매칭 status, 매너 매세지 전송 status 변경 및 새로운 채팅방 생성, 기존 채팅방이 없는 경우")
        @Test
        void matchingFoundSucceedWhenChatroomNotExists() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.FOUND);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.FOUND);

            // 기존 채팅방이 없는 상태
            assertThat(chatroomRepository.findAll()).isEmpty();

            Member systemMember = createMember("systemMember@gmail.com", "systemMember");
            given(memberRepository.findById(0L)).willReturn(Optional.of(systemMember));

            // when
            String chatUuid = matchingFacadeService.matchingSuccess(matchingRecord.getMatchingUuid(),
                    targetMatchingRecord.getMatchingUuid());

            // then
            assertThat(matchingRecord.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
            assertThat(matchingRecord.getMannerMessageSent()).isEqualTo(MannerMessageStatus.NOT_SENT);
            assertThat(targetMatchingRecord.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
            assertThat(targetMatchingRecord.getMannerMessageSent()).isEqualTo(MannerMessageStatus.NOT_SENT);

            // 새로운 채팅방이 생성되었는지 검증
            Chatroom newChatroom = chatroomRepository.findByUuid(chatUuid).orElseThrow();
            assertThat(newChatroom).isNotNull();
            assertThat(newChatroom.getUuid()).isEqualTo(chatUuid);

            // lastJoinDate 업데이트 검증
            MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(member.getId(),
                    newChatroom.getId()).orElseThrow();
            assertThat(memberChatroom.getLastJoinDate()).isNotNull();

            MemberChatroom targetMemberChatroom =
                    memberChatroomRepository.findByMemberIdAndChatroomId(targetMember.getId(), newChatroom.getId()).orElseThrow();
            assertThat(targetMemberChatroom.getLastJoinDate()).isNotNull();

            // socket service 호출 여부 검증 (두 회원이 새로운 채팅방에 입장해야 하므로 두 번 호출)
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(socketService, times(2)).joinSocketToChatroom(any(Long.class), eq(newChatroom.getUuid())));

            // 매칭 시스템 메시지 생성 검증
            List<Chat> systemChats = chatRepository.findByChatroomIdAndFromMemberId(newChatroom.getId(),
                    systemMember.getId());
            assertThat(systemChats).hasSize(2);
            systemChats.forEach(systemChat -> assertThat(systemChat.getContents()).isEqualTo(SystemMessageType.MATCH_SUCCESS_MESSAGE.getMessage()));
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

    private void blindMember(Member member) {
        member.updateBlind(true);
        memberRepository.save(member);
    }

    private Block blockMember(Member member, Member targetMember) {
        return blockRepository.save(Block.create(member, targetMember));
    }

    private Member createMatchingMember(String email, String gameName, String tag, Tier tier, int gameRank,
                                        Mike mike, Position mainP, Position subP, java.util.List<Position> wantP,
                                        int mannerLevel) {
        Member member1 = Member.builder()
                .email(email)
                .password("testPassword")
                .profileImage(1)
                .loginType(LoginType.GENERAL)
                .gameName(gameName)
                .tag(tag)
                .soloTier(tier)
                .soloRank(0)
                .soloWinRate(0.0)
                .soloGameCount(0)
                .freeTier(tier)
                .freeRank(gameRank)
                .freeWinRate(0.0)
                .freeGameCount(0)
                .isAgree(true)
                .build();
        member1.updateMike(mike);
        member1.updatePosition(mainP, subP, wantP);
        member1.updateMannerLevel(mannerLevel);
        return memberRepository.save(member1);
    }

    private MatchingRecord createMatchingRecord(GameMode mode, MatchingType type, Member member,
                                                MatchingStatus status) {
        MatchingRecord matchingRecord = MatchingRecord.create(mode, type, member);
        matchingRecord.updateStatus(status);
        return matchingRecordRepository.save(matchingRecord);
    }

}
