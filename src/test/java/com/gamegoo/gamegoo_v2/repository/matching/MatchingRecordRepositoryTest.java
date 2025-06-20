package com.gamegoo.gamegoo_v2.repository.matching;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchingRecordRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private MatchingRecordRepository matchingRecordRepository;

    private Member targetMember;

    @BeforeEach
    void setUp() {
        targetMember = createMember("target@gmail.com", "targetMember");
    }

    @Nested
    @DisplayName("매칭 uuid로 상대 회원 조회")
    class FindTargetMemberByUuid {

        @DisplayName("해당 매칭 레코드의 상대 매칭 레코드가 지정되지 않은 경우, Optional empty를 반환한다.")
        @Test
        void findTargetMemberByUuidWhenNoTarget() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(member, GameMode.FAST);

            // when
            Optional<Member> result = matchingRecordRepository.findTargetMemberByUuid(
                    matchingRecord.getMatchingUuid());

            // then
            assertThat(result).isEmpty();
        }

        @DisplayName("해당 매칭 레코드의 상대 매칭 레코드가 지정된 경우, 상대 회원 엔티티를 반환한다.")
        @Test
        void findTargetMemberByUuid() {
            // given
            MatchingRecord matchingRecord = createMatchingRecord(member, GameMode.FAST);
            MatchingRecord targetMatchingRecord = createMatchingRecord(targetMember, GameMode.FAST);

            matchingRecord.updateTargetMatchingRecord(targetMatchingRecord);
            targetMatchingRecord.updateTargetMatchingRecord(matchingRecord);

            em.flush();
            em.clear();

            // when
            Optional<Member> result = matchingRecordRepository.findTargetMemberByUuid(
                    matchingRecord.getMatchingUuid());

            // then
            Member targetMember = result.orElseThrow();
            assertThat(targetMember.getId()).isEqualTo(targetMember.getId());
        }

    }

    private MatchingRecord createMatchingRecord(Member member, GameMode gameMode) {
        return em.persist(MatchingRecord.builder()
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

}
