package com.gamegoo.gamegoo_v2.repository.report;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportTypeMapping;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportRepository;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportTypeMappingRepository;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportTypeMappingRepository reportTypeMappingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    private Member fromMember;
    private Member toMember;
    private Member bannedMember;
    private Board board;
    private Board deletedBoard;

    @BeforeEach
    void setUp() {
        fromMember = createMember("from@gmail.com", "fromMember");
        toMember = createMember("to@gmail.com", "toMember");
        bannedMember = createMember("banned@gmail.com", "bannedMember");
        
        board = createBoard(toMember, false);
        deletedBoard = createBoard(fromMember, true);
    }

    @AfterEach
    void tearDown() {
        reportTypeMappingRepository.deleteAllInBatch();
        reportRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("신고 존재 여부 확인")
    class ExistsByFromMemberIdAndToMemberIdAndCreatedAtBetweenTest {

        @DisplayName("오늘 날짜에 신고가 존재하는 경우 true 반환")
        @Test
        void existsByFromMemberIdAndToMemberIdAndCreatedAtBetweenReturnsTrueWhenExists() {
            // given
            reportRepository.save(Report.create(fromMember, toMember, "content", ReportPath.CHAT, null));
            
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            // when
            boolean exists = reportRepository.existsByFromMemberIdAndToMemberIdAndCreatedAtBetween(
                    fromMember.getId(), toMember.getId(), startOfDay, endOfDay);

            // then
            assertThat(exists).isTrue();
        }

        @DisplayName("오늘 날짜에 신고가 존재하지 않는 경우 false 반환")
        @Test
        void existsByFromMemberIdAndToMemberIdAndCreatedAtBetweenReturnsFalseWhenNotExists() {
            // given
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            // when
            boolean exists = reportRepository.existsByFromMemberIdAndToMemberIdAndCreatedAtBetween(
                    fromMember.getId(), toMember.getId(), startOfDay, endOfDay);

            // then
            assertThat(exists).isFalse();
        }

        @DisplayName("다른 회원 조합의 신고가 존재하는 경우 false 반환")
        @Test
        void existsByFromMemberIdAndToMemberIdAndCreatedAtBetweenReturnsFalseWhenDifferentMembers() {
            // given
            reportRepository.save(Report.create(fromMember, bannedMember, "content", ReportPath.CHAT, null));
            
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            // when
            boolean exists = reportRepository.existsByFromMemberIdAndToMemberIdAndCreatedAtBetween(
                    fromMember.getId(), toMember.getId(), startOfDay, endOfDay);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("신고 검색")
    class SearchReportsTest {

        private Report report1;
        private Report report2;
        private Report report3;

        @BeforeEach
        void setUpReports() {
            report1 = reportRepository.save(Report.create(fromMember, toMember, "욕설 신고", ReportPath.CHAT, null));
            report2 = reportRepository.save(Report.create(toMember, bannedMember, "스팸 신고", ReportPath.BOARD, board));
            report3 = reportRepository.save(Report.create(fromMember, bannedMember, "부정행위 신고", ReportPath.PROFILE, deletedBoard));

            reportTypeMappingRepository.save(ReportTypeMapping.create(report1, 4)); // 욕설/ 혐오/ 차별적 표현
            reportTypeMappingRepository.save(ReportTypeMapping.create(report2, 6)); // 불쾌한 표현
            reportTypeMappingRepository.save(ReportTypeMapping.create(report3, 1)); // 스팸
        }

        @DisplayName("검색 조건이 없는 경우 모든 신고 반환")
        @Test
        void searchReportsWithoutConditions() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder().build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(3);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report1.getId(), report2.getId(), report3.getId());
        }

        @DisplayName("신고자 키워드로 검색")
        @Test
        void searchReportsByReporterKeyword() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reporterKeyword("fromMember")
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report1.getId(), report3.getId());
        }

        @DisplayName("피신고자 키워드로 검색")
        @Test
        void searchReportsByReportedMemberKeyword() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reportedMemberKeyword("bannedMember")
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report2.getId(), report3.getId());
        }

        @DisplayName("내용 키워드로 검색")
        @Test
        void searchReportsByContentKeyword() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .contentKeyword("욕설")
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(report1.getId());
        }

        @DisplayName("신고 경로로 검색")
        @Test
        void searchReportsByReportPaths() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reportPaths(List.of(ReportPath.CHAT, ReportPath.BOARD))
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report1.getId(), report2.getId());
        }

        @DisplayName("신고 유형으로 검색")
        @Test
        void searchReportsByReportTypes() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reportTypes(List.of(4, 6)) // 욕설/ 혐오/ 차별적 표현, 불쾌한 표현
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report1.getId(), report2.getId());
        }

        @DisplayName("날짜 범위로 검색")
        @Test
        void searchReportsByDateRange() {
            // given
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);
            
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(3);
        }

        @DisplayName("시작 날짜만으로 검색")
        @Test
        void searchReportsByStartDateOnly() {
            // given
            LocalDateTime startDate = LocalDateTime.now().minusHours(1);
            
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .startDate(startDate)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(3);
        }

        @DisplayName("종료 날짜만으로 검색")
        @Test
        void searchReportsByEndDateOnly() {
            // given
            LocalDateTime endDate = LocalDateTime.now().plusHours(1);
            
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .endDate(endDate)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(3);
        }

        @DisplayName("정확한 신고 횟수로 검색")
        @Test
        void searchReportsByExactReportCount() {
            // given
            // bannedMember는 2번 신고당함
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reportCountExact(2)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report2.getId(), report3.getId());
        }

        @DisplayName("최소 신고 횟수로 검색")
        @Test
        void searchReportsByMinReportCount() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reportCountMin(2)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report2.getId(), report3.getId());
        }

        @DisplayName("최대 신고 횟수로 검색")
        @Test
        void searchReportsByMaxReportCount() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reportCountMax(1)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(report1.getId());
        }

        @DisplayName("신고 횟수 범위로 검색")
        @Test
        void searchReportsByReportCountRange() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reportCountMin(1)
                    .reportCountMax(2)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(3);
        }

        @DisplayName("삭제된 게시글 필터 - 삭제된 게시글만")
        @Test
        void searchReportsWithDeletedBoardsOnly() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .isDeleted(true)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(report3.getId());
        }

        @DisplayName("삭제된 게시글 필터 - 삭제되지 않은 게시글만")
        @Test
        void searchReportsWithNonDeletedBoardsOnly() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .isDeleted(false)
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report1.getId(), report2.getId());
        }


        @DisplayName("복합 조건으로 검색")
        @Test
        void searchReportsByMultipleConditions() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .reporterKeyword("fromMember")
                    .reportPaths(List.of(ReportPath.CHAT))
                    .contentKeyword("욕설")
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(report1.getId());
        }

        @DisplayName("조건에 맞는 결과가 없는 경우 빈 리스트 반환")
        @Test
        void searchReportsReturnsEmptyListWhenNoMatch() {
            // given
            ReportSearchRequest request = ReportSearchRequest.builder()
                    .contentKeyword("존재하지않는키워드")
                    .build();

            // when
            List<Report> results = reportRepository.searchReports(request, Pageable.unpaged());

            // then
            assertThat(results).isEmpty();
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

    private Board createBoard(Member member, boolean deleted) {
        Board board = Board.builder()
                .member(member)
                .gameMode(GameMode.SOLO)
                .mainP(Position.ADC)
                .subP(Position.JUNGLE)
                .wantP(List.of(Position.ADC))
                .mike(Mike.AVAILABLE)
                .content("content")
                .boardProfileImage(1)
                .deleted(deleted)
                .build();
        
        return boardRepository.save(board);
    }
}