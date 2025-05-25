package com.gamegoo.gamegoo_v2.service.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.service.MatchingScoreCalculator;
import com.gamegoo.gamegoo_v2.matching.service.MatchingStrategyProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class MatchingStrategyProcessorTest {

    @Autowired
    MatchingStrategyProcessor matchingStrategyProcessor;

    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("정밀 매칭 우선순위 계산(SOLO)")
    void testCalculatePrecisePriority() {
        // given
        Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
        Member member2 = createMember("user2@gmail.com", Tier.GOLD, false);

        // 매너 점수 차이: 4*2점
        member1.updateMannerLevel(4);
        member2.updateMannerLevel(2);

        // when
        MatchingRecord record1 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.SOLO, MatchingType.BASIC, member2);

        int result = matchingStrategyProcessor.calculatePrecisePriority(record1, record2);

        // then
        int expectedPriority = 67 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4);
        assertThat(result).isEqualTo(expectedPriority);
    }

    @Test
    @DisplayName("정밀 매칭 우선순위 계산(FAST)")
    void testCalculatePrecisePriority_FAST() {
        // given
        // Tier 같음(가정)
        Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
        Member member2 = createMember("user2@gmail.com", Tier.GOLD, false);

        // 매너 점수 차이: 3*4점
        member1.updateMannerLevel(3);
        member2.updateMannerLevel(0);

        MatchingRecord record1 = createMatchingRecord(GameMode.FAST, MatchingType.PRECISE, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.FAST, MatchingType.BASIC, member2);

        // when
        int result = matchingStrategyProcessor.calculatePrecisePriority(record1, record2);

        // then
        int expectedPriority = 25 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4);
        assertThat(result).isEqualTo(expectedPriority);
    }

    @Test
    @DisplayName("정밀 매칭 우선순위 계산(FREE)")
    void testCalculatePrecisePriority_FREE() {
        // given
        Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
        Member member2 = createMember("user2@gmail.com", Tier.GOLD, false);

        // 매너 점수 차이: 3*4점
        member1.updateMannerLevel(3);
        member2.updateMannerLevel(0);

        MatchingRecord record1 = createMatchingRecord(GameMode.FREE, MatchingType.PRECISE, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.FREE, MatchingType.BASIC, member2);

        // when
        int result = matchingStrategyProcessor.calculatePrecisePriority(record1, record2);

        // then
        int expectedPriority = 65 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4);
        assertThat(result).isEqualTo(expectedPriority);
    }

    @Test
    @DisplayName("정밀 매칭 우선순위 계산(ARAM)")
    void testCalculatePrecisePriority_ARAM() {
        // given
        Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
        Member member2 = createMember("user2@gmail.com", Tier.GOLD, false);

        // 매너 점수 차이: 3*4점
        member1.updateMannerLevel(3);
        member2.updateMannerLevel(0);

        MatchingRecord record1 = createMatchingRecord(GameMode.ARAM, MatchingType.PRECISE, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.ARAM, MatchingType.BASIC, member2);

        // when
        int result = matchingStrategyProcessor.calculatePrecisePriority(record1, record2);

        // then
        int expectedPriority = 19 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4);
        assertThat(result).isEqualTo(expectedPriority);
    }


    @Test
    @DisplayName("개인랭크 우선순위 계산")
    void testCalculateSoloPriorityWithStateChanges() {
        // given
        Member member1 = createMember("user1@gmail.com", Tier.DIAMOND, true);
        Member member2 = createMember("user2@gmail.com", Tier.PLATINUM, true);

        // 마이크 차이 : 5점
        member1.updateMike(Mike.AVAILABLE);
        member2.updateMike(Mike.UNAVAILABLE);

        // 포지션 점수 차이 : 2+3=5점
        member1.updatePosition(Position.TOP, Position.MID, List.of(Position.ANY));
        member2.updatePosition(Position.JUNGLE, Position.SUP, List.of(Position.MID));

        // 매너 점수 차이 : 8점
        member1.updateMannerLevel(5);
        member2.updateMannerLevel(3);

        // when
        MatchingRecord record1 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member2);
        int result = matchingStrategyProcessor.calculateSoloPriority(record1, record2);

        // then
        int expectedPriority = 16 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4) +
                (40 - Math.abs(record1.getTier().ordinal() * 4 + record1.getGameRank() -
                        record2.getTier().ordinal() * 4 - record2.getGameRank())) +
                (record1.getMike().equals(record2.getMike()) ? 5 : 0) + getPositionExpectedPriority(member1, member2);

        assertThat(result).isEqualTo(expectedPriority);
    }


    @Test
    @DisplayName("자유랭크 우선순위 계산")
    void testCalculateFreePriority() {
        // given
        // 랭크 점수 차이 : 4점
        Member member1 = createMember("user1@gmail.com", Tier.BRONZE, true);
        Member member2 = createMember("user2@gmail.com", Tier.SILVER, true);

        // 포지션 점수 차이 : 6점
        member1.updatePosition(Position.MID, Position.SUP, List.of(Position.ANY));
        member2.updatePosition(Position.JUNGLE, Position.ADC, List.of(Position.MID));

        // 매너 점수 차이 : 4점
        member1.updateMannerLevel(3);
        member2.updateMannerLevel(2);

        // 마이크 점수 차이 : 3점
        member1.updateMike(Mike.AVAILABLE);
        member2.updateMike(Mike.UNAVAILABLE);

        // when
        MatchingRecord record1 = createMatchingRecord(GameMode.FREE, MatchingType.BASIC, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.FREE, MatchingType.BASIC, member2);
        int result = matchingStrategyProcessor.calculateFreePriority(record1, record2);

        // then
        int expectedPriority = 16 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4) +
                (40 - Math.abs(record1.getTier().ordinal() * 4 + record1.getGameRank() -
                        record2.getTier().ordinal() * 4 - record2.getGameRank())) +
                (record1.getMike().equals(record2.getMike()) ? 3 : 0) + getPositionExpectedPriority(member1, member2);

        assertThat(result).isEqualTo(expectedPriority);
    }

    @Test
    @DisplayName("빠른대전 우선순위 계산")
    void testCalculateFastPriority() {
        // given
        // 랭크 점수 차이 : 4점
        Member member1 = createMember("user1@gmail.com", Tier.BRONZE, true);
        Member member2 = createMember("user2@gmail.com", Tier.SILVER, true);

        // 포지션 점수 차이 : 6점
        member1.updatePosition(Position.MID, Position.SUP, List.of(Position.ANY));
        member2.updatePosition(Position.JUNGLE, Position.ADC, List.of(Position.MID));

        // 매너 점수 차이 : 4점
        member1.updateMannerLevel(3);
        member2.updateMannerLevel(2);

        // 마이크 점수 차이 : 3점
        member1.updateMike(Mike.AVAILABLE);
        member2.updateMike(Mike.UNAVAILABLE);

        // when
        MatchingRecord record1 = createMatchingRecord(GameMode.FREE, MatchingType.BASIC, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.FREE, MatchingType.BASIC, member2);
        int result = matchingStrategyProcessor.calculateFastPriority(record1, record2);

        // then
        int expectedPriority = 16 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4) +
                (record1.getMike().equals(record2.getMike()) ? 3 : 0) + getPositionExpectedPriority(member1, member2);

        assertThat(result).isEqualTo(expectedPriority);
    }


    @Test
    @DisplayName("칼바람 모드 우선순위 계산 - 다양한 마이크 설정")
    void testCalculateAramPriority() {
        // given
        Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
        Member member2 = createMember("user2@gmail.com", Tier.GOLD, false);

        // 마이크 점수 차이 : 3점
        member1.updateMike(Mike.AVAILABLE);
        member2.updateMike(Mike.UNAVAILABLE);

        // when
        MatchingRecord record1 = createMatchingRecord(GameMode.ARAM, MatchingType.BASIC, member1);
        MatchingRecord record2 = createMatchingRecord(GameMode.ARAM, MatchingType.BASIC, member2);
        int result = matchingStrategyProcessor.calculateAramPriority(record1, record2);

        // then
        int expectedPriority = 16 - (Math.abs(record1.getMannerLevel() - record2.getMannerLevel()) * 4) +
                (record1.getMike().equals(record2.getMike()) ? 3 : 0);

        assertThat(result).isEqualTo(expectedPriority);
    }


    @Nested
    @DisplayName("정밀 매칭 검증")
    class ValidatePreciseMatchingTests {

        @Test
        @DisplayName("성공 케이스")
        void testValidatePreciseMatching_Success() {
            Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
            member1.updatePosition(Position.TOP, Position.MID, List.of(Position.MID));
            Member member2 = createMember("user2@gmail.com", Tier.GOLD, true);
            member2.updatePosition(Position.MID, Position.JUNGLE, List.of(Position.MID));
            MatchingRecord record1 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member1);
            MatchingRecord record2 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member2);

            boolean isValid = matchingStrategyProcessor.validatePreciseMatching(record1, record2);
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("실패 케이스: 티어 차이")
        void testValidatePreciseMatching_Fail_Tier() {
            // given
            Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
            Member member2 = createMember("user2@gmail.com", Tier.SILVER, true);

            // when
            MatchingRecord record1 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member1);
            MatchingRecord record2 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member2);

            boolean isValid = matchingStrategyProcessor.validatePreciseMatching(record1, record2);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("실패 케이스: 포지션 불일치")
        void testValidatePreciseMatching_Fail_Position() {
            // given
            Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
            member1.updatePosition(Position.TOP, Position.MID, List.of(Position.MID));
            Member member2 = createMember("user2@gmail.com", Tier.GOLD, true);
            member2.updatePosition(Position.JUNGLE, Position.SUP, List.of(Position.MID));

            // when
            MatchingRecord record1 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member1);
            MatchingRecord record2 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member2);

            boolean isValid = matchingStrategyProcessor.validatePreciseMatching(record1, record2);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("실패 케이스: 마이크 불일치")
        void testValidatePreciseMatching_Fail_Mike() {
            // given
            Member member1 = createMember("user1@gmail.com", Tier.GOLD, true);
            member1.updateMike(Mike.UNAVAILABLE);
            Member member2 = createMember("user2@gmail.com", Tier.GOLD, false);
            member2.updateMike(Mike.AVAILABLE);

            // when
            MatchingRecord record1 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member1);
            MatchingRecord record2 = createMatchingRecord(GameMode.SOLO, MatchingType.PRECISE, member2);

            boolean isValid = matchingStrategyProcessor.validatePreciseMatching(record1, record2);

            // then
            assertThat(isValid).isFalse();
        }

    }

    private Member createMember(String email, Tier tier, boolean hasMike) {
        Member member = Member.createForGeneral(email, "password123", LoginType.GENERAL, "gameUser", "TAG",
                tier, 4, 55.0, tier, 4, 55.0, 100, 100, true);

        if (hasMike) {
            member.updateMike(Mike.AVAILABLE);
        } else {
            member.updateMike(Mike.UNAVAILABLE);
        }

        return memberRepository.save(member);

    }

    private MatchingRecord createMatchingRecord(GameMode mode, MatchingType type, Member member) {
        return MatchingRecord.create(mode, type, member);
    }

    private int getPositionExpectedPriority(Member member1, Member member2) {
        // 포지션 expectedPriority 계산
        int positionPriority = 0;

        positionPriority += MatchingScoreCalculator.getPositionPriority(
                member1.getWantPositions().get(0), member2.getMainP(), member2.getSubP(),
                3, 2, 1
        );

        positionPriority += MatchingScoreCalculator.getPositionPriority(
                member2.getWantPositions().get(0), member1.getMainP(), member1.getSubP(),
                3, 2, 1
        );
        return positionPriority;
    }

}
