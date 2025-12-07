package com.gamegoo.gamegoo_v2.repository.matching;

import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.repository.MatchingRecordRepository;
import com.gamegoo.gamegoo_v2.repository.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MatchingRecordRepositoryCustomImplTest extends RepositoryTestSupport {

    @Autowired
    private EntityManager em;

    @Autowired
    private MatchingRecordRepository matchingRecordRepository;


    @ParameterizedTest(name = "{4}")
    @MethodSource("provideMatchingRecordScenarios")
    @DisplayName("현재 시각 기준 5분 이내 생성된 PENDING 매칭만 조회된다 - 파라미터 테스트")
    void findValidMatchingRecords_withinFiveMinutes_only_parameterized(
            long minutesAgo,
            MatchingStatus status,
            boolean isSelf,
            boolean expectedIncluded,
            String description
    ) {
        // given
        LocalDateTime baseTime = LocalDateTime.now();
        GameMode gameMode = GameMode.ARAM;

        Member member = createMember("iron@test.com", "iron");
        Member otherMember = createMember("gold@test.com", "gold");

        // 내 매칭
        MatchingRecord myRecord = createMatchingRecord(
                member,
                gameMode,
                MatchingType.BASIC,
                MatchingStatus.PENDING
        );

        // 상대 매칭
        MatchingRecord testMatchingRecord = isSelf
                ? createMatchingRecord(member, gameMode, MatchingType.BASIC, status)
                : createMatchingRecord(otherMember, gameMode, MatchingType.BASIC, status);

        // 상대 매칭의 created_at 강제 수정
        em.flush();
        em.createQuery("update MatchingRecord m set m.createdAt = :time where m.matchingUuid = :uuid")
                .setParameter("time", baseTime.minusMinutes(minutesAgo))
                .setParameter("uuid", testMatchingRecord.getMatchingUuid())
                .executeUpdate();
        em.clear();

        // when
        List<MatchingRecord> result = matchingRecordRepository.findValidMatchingRecords(
                baseTime,
                gameMode,
                member.getId()
        );

        // then
        if (expectedIncluded) {
            assertThat(result)
                    .extracting(MatchingRecord::getMatchingUuid)
                    .contains(testMatchingRecord.getMatchingUuid());
        } else {
            assertThat(result)
                    .extracting(MatchingRecord::getMatchingUuid)
                    .doesNotContain(testMatchingRecord.getMatchingUuid());
        }
    }

    private static Stream<Arguments> provideMatchingRecordScenarios() {
        return Stream.of(
                Arguments.of(4L, MatchingStatus.PENDING, false, true, "4분전 PENDING, 상대 → 포함"),
                Arguments.of(6L, MatchingStatus.PENDING, false, false, "6분전 PENDING, 상대 → 제외"),
                Arguments.of(1L, MatchingStatus.FOUND, false, false, "1분전 FOUND → 상태 제외"),
                Arguments.of(1L, MatchingStatus.PENDING, true, false, "본인이 만든 매칭 → 제외")
        );
    }

    private MatchingRecord createMatchingRecord(
            Member member,
            GameMode gameMode,
            MatchingType matchingType,
            MatchingStatus status
    ) {
        MatchingRecord record = MatchingRecord.create(gameMode, matchingType, member);
        record.updateStatus(status);

        em.persist(record);
        return record;
    }

}
