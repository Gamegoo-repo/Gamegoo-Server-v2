package com.gamegoo.gamegoo_v2.service.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import com.gamegoo.gamegoo_v2.matching.dto.response.MatchingMemberInfoResponse;
import com.gamegoo.gamegoo_v2.matching.dto.response.PriorityListResponse;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import com.gamegoo.gamegoo_v2.matching.service.MatchingService;
import com.gamegoo.gamegoo_v2.matching.service.MatchingStrategyProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class MatchingServiceTest {

    @Autowired
    MatchingService matchingService;

    @Autowired
    MatchingStrategyProcessor matchingStrategyProcessor;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MatchingRecordRepository matchingRecordRepository;

    private Member member;

    @AfterEach
    void tearDown() {
        matchingRecordRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        member = createMember("user1@gmail.com", "User1", "Tag1", Tier.GOLD, 2, true, Position.ADC, Position.MID,
                Position.SUP, 2);
    }

    @Nested
    @DisplayName("매칭 우선순위 리스트 계산 후 조회")
    class getMatchingPriorityList {

        @DisplayName("매칭 리스트 조회 성공 : 대기자가 없는 경우")
        @Test
        void getMatchingPriorityListSucceedsWhenNoUser() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member,
                    MatchingStatus.PENDING);

            // when
            PriorityListResponse priorityListResponse = matchingService.calculatePriorityList(matchingRecord,
                    new ArrayList<>());

            // then
            MatchingMemberInfoResponse matchingMemberInfoResponse = MatchingMemberInfoResponse.of(member,
                    matchingRecord.getMatchingUuid());


            assertThat(priorityListResponse.getMyPriorityList()).isEqualTo(new ArrayList<>());
            assertThat(priorityListResponse.getOtherPriorityList()).isEqualTo(new ArrayList<>());
            assertThat(priorityListResponse.getMyMatchingInfo()).isEqualTo(matchingMemberInfoResponse);
        }

        @DisplayName("매칭 리스트 조회 성공 : 랜덤 대기자 20명")
        @Test
        void validateMatchingPriorityListsSucceeds() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member,
                    MatchingStatus.PENDING);

            Random random = new Random();
            List<MatchingRecord> allMatchingRecords = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
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

                Member targetMember = createMember(email, gameName, tag, tier, gameRank, hasMike, mainP, subP, wantP,
                        mannerLevel);
                MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                        targetMember, MatchingStatus.PENDING);

                allMatchingRecords.add(targetMatchingRecord);
            }

            // when
            PriorityListResponse priorityListResponse = matchingService.calculatePriorityList(matchingRecord,
                    allMatchingRecords);

            // 예상되는 우선순위 리스트를 수동으로 생성
            List<PriorityValue> expectedMyPriorityList = new ArrayList<>();
            List<PriorityValue> expectedOtherPriorityList = new ArrayList<>();

            for (MatchingRecord otherRecord : allMatchingRecords) {
                Long otherMemberId = otherRecord.getMember().getId();
                if (!matchingRecord.getMember().getId().equals(otherMemberId)) {
                    int otherPriority = matchingService.calculatePriority(matchingRecord.getGameMode(),
                            matchingRecord, otherRecord);
                    expectedMyPriorityList.add(PriorityValue.of(otherMemberId, otherRecord.getMatchingUuid(),
                            otherPriority));

                    int myPriority = matchingService.calculatePriority(matchingRecord.getGameMode(), otherRecord,
                            matchingRecord);
                    expectedOtherPriorityList.add(PriorityValue.of(matchingRecord.getMember().getId(),
                            matchingRecord.getMatchingUuid(), myPriority));
                }
            }

            // 정렬 (ID 기준으로 정렬하여 비교)
            Comparator<PriorityValue> priorityComparator = Comparator.comparing(PriorityValue::getMemberId);
            expectedMyPriorityList.sort(priorityComparator);
            expectedOtherPriorityList.sort(priorityComparator);
            priorityListResponse.getMyPriorityList().sort(priorityComparator);
            priorityListResponse.getOtherPriorityList().sort(priorityComparator);

            // then - 우선순위 리스트가 예상과 같은지 검증
            assertThat(priorityListResponse.getMyPriorityList()).isEqualTo(expectedMyPriorityList);
            assertThat(priorityListResponse.getOtherPriorityList()).isEqualTo(expectedOtherPriorityList);
        }

    }

    @Nested
    @DisplayName("매칭 우선순위 점수 계산")
    class getMatchingPriority {

        @DisplayName("매칭 우선순위 점수 계산 : 개인 랭크")
        @Test
        void getMatchingPrioritySucceedsWhenSoloGameMode() {
            // given
            MatchingRecord myMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            Member targetMember = createMember("target@gmail.com", "target", "tag", Tier.SILVER, 1, true,
                    Position.SUP, Position.TOP, Position.ADC, 3);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when
            int priority = matchingService.calculatePriority(myMatchingRecord.getGameMode(), myMatchingRecord,
                    targetMatchingRecord);

            // then
            int expectedPriority = matchingStrategyProcessor.calculateSoloPriority(myMatchingRecord,
                    targetMatchingRecord);
            assertThat(priority).isEqualTo(expectedPriority);
        }

        @DisplayName("매칭 우선순위 점수 계산 : 자유랭크")
        @Test
        void getMatchingPrioritySucceedsWhenFreeGameMode() {
            // given
            MatchingRecord myMatchingRecord = createMatchingRecord(GameMode.FREE, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            Member targetMember = createMember("target@gmail.com", "target", "tag", Tier.SILVER, 1, true,
                    Position.SUP, Position.TOP, Position.ADC, 3);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.FREE, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when
            int priority = matchingService.calculatePriority(myMatchingRecord.getGameMode(), myMatchingRecord,
                    targetMatchingRecord);

            // then
            int expectedPriority = matchingStrategyProcessor.calculateFreePriority(myMatchingRecord,
                    targetMatchingRecord);
            assertThat(priority).isEqualTo(expectedPriority);
        }


        @DisplayName("매칭 우선순위 점수 계산 : 칼바람")
        @Test
        void getMatchingPrioritySucceedsWhenAramGameMode() {
            // given
            MatchingRecord myMatchingRecord = createMatchingRecord(GameMode.ARAM, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            Member targetMember = createMember("target@gmail.com", "target", "tag", Tier.SILVER, 1, true,
                    Position.ANY, Position.ANY, Position.ANY, 3);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.ARAM, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when
            int priority = matchingService.calculatePriority(myMatchingRecord.getGameMode(), myMatchingRecord,
                    targetMatchingRecord);

            // then
            int expectedPriority = matchingStrategyProcessor.calculateAramPriority(myMatchingRecord,
                    targetMatchingRecord);
            assertThat(priority).isEqualTo(expectedPriority);
        }

        @DisplayName("매칭 우선순위 점수 계산 : 빠른 대전")
        @Test
        void getMatchingPrioritySucceedsWhenFastGameMode() {
            // given
            MatchingRecord myMatchingRecord = createMatchingRecord(GameMode.FAST, MatchingType.BASIC, member,
                    MatchingStatus.PENDING);
            Member targetMember = createMember("target@gmail.com", "target", "tag", Tier.SILVER, 1, true,
                    Position.SUP, Position.TOP, Position.ADC, 3);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.FAST, MatchingType.BASIC,
                    targetMember, MatchingStatus.PENDING);

            // when
            int priority = matchingService.calculatePriority(myMatchingRecord.getGameMode(), myMatchingRecord,
                    targetMatchingRecord);

            // then
            int expectedPriority = matchingStrategyProcessor.calculateFastPriority(myMatchingRecord,
                    targetMatchingRecord);
            assertThat(priority).isEqualTo(expectedPriority);
        }

        @DisplayName("매칭 우선순위 점수 계산 : 정밀 매칭")
        @Test
        void getMatchingPrioritySucceedsWhenPreciseMatchingType() {
            // given
            MatchingRecord myMatchingRecord = createMatchingRecord(GameMode.FAST, MatchingType.PRECISE, member,
                    MatchingStatus.PENDING);
            Member targetMember = createMember("target@gmail.com", "target", "tag", Tier.SILVER, 1, true,
                    Position.SUP, Position.TOP, Position.ADC, 3);
            MatchingRecord targetMatchingRecord = createMatchingRecord(GameMode.FAST, MatchingType.PRECISE,
                    targetMember, MatchingStatus.PENDING);

            // when
            int priority = matchingService.calculatePriority(myMatchingRecord.getGameMode(), myMatchingRecord,
                    targetMatchingRecord);

            // then
            int expectedPriority = 0;
            if (matchingStrategyProcessor.validatePreciseMatching(myMatchingRecord, targetMatchingRecord)) {
                expectedPriority = matchingStrategyProcessor.calculatePrecisePriority(myMatchingRecord,
                        targetMatchingRecord);
            }
            assertThat(priority).isEqualTo(expectedPriority);
        }

    }

    @DisplayName("대기 중인 매칭 리스트 조회")
    @Test
    void getPendingMatchingRecordLists() {
        // given
        Random random = new Random();
        GameMode gameMode = GameMode.values()[random.nextInt(GameMode.values().length)];
        List<MatchingRecord> allMatchingRecords = new ArrayList<>();

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

            Member targetMember = createMember(email, gameName, tag, tier, gameRank, hasMike, mainP, subP, wantP,
                    mannerLevel);
            MatchingRecord targetMatchingRecord = createMatchingRecord(randomGameMode, randomMatchingType, targetMember,
                    randomMatchingStatus);

            // MatchingRecord 리스트에 저장
            allMatchingRecords.add(targetMatchingRecord);
        }

        // when
        List<MatchingRecord> matchingRecords = matchingService.getPendingMatchingRecords(gameMode);

        // then
        List<MatchingRecord> expectedMatchingRecords =
                matchingRecordRepository.findValidMatchingRecords(LocalDateTime.now().minusMinutes(5), gameMode);
        assertThat(matchingRecords.size()).isEqualTo(expectedMatchingRecords.size());
    }

    @DisplayName("매칭 기록 생성")
    @Test
    void createMatchingRecord() {
        // when
        MatchingRecord matchingRecord = matchingService.createMatchingRecord(member, MatchingType.BASIC, GameMode.FREE);

        // then
        assertThat(matchingRecord).isNotNull();
        assertThat(matchingRecord.getGameMode()).isEqualTo(GameMode.FREE);
        assertThat(matchingRecord.getMatchingType()).isEqualTo(MatchingType.BASIC);
        assertThat(matchingRecord.getMember()).isEqualTo(member);
    }

    private Member createMember(String email, String gameName, String tag, Tier tier, int gameRank, boolean hasMike,
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
