package com.gamegoo.gamegoo_v2.service.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import com.gamegoo.gamegoo_v2.matching.dto.response.MatchingMemberInfoResponse;
import com.gamegoo.gamegoo_v2.matching.dto.response.PriorityListResponse;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import com.gamegoo.gamegoo_v2.matching.service.MatchingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
    private MemberRepository memberRepository;

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
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member);

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
        void validateMatchingPriorityLists() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member);

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

                Member tempMember = createMember(email, gameName, tag, tier, gameRank, hasMike, mainP, subP, wantP,
                        mannerLevel);
                MatchingRecord tempMatchingRecord = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC,
                        tempMember);

                allMatchingRecords.add(tempMatchingRecord);
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

    private Member createMember(String email, String gameName, String tag, Tier tier, int gameRank, boolean hasMike,
                                Position mainP, Position subP,
                                Position wantP, int mannerLevel) {

        Member member = Member.create(email, "password123", LoginType.GENERAL, gameName, tag, tier, gameRank, 55.0,
                100, hasMike);
        member.updateMannerLevel(mannerLevel);
        member.updatePosition(mainP, subP, wantP);
        return memberRepository.save(member);
    }

    private MatchingRecord createMatchingRecord(GameMode mode, MatchingType type, Member member) {
        MatchingRecord matchingRecord = MatchingRecord.create(mode, type, member);
        return matchingRecordRepository.save(matchingRecord);
    }

}
