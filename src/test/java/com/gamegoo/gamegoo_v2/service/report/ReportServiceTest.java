package com.gamegoo.gamegoo_v2.service.report;

import com.gamegoo.gamegoo_v2.account.member.domain.LoginType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportTypeMapping;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportRepository;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportTypeMappingRepository;
import com.gamegoo.gamegoo_v2.content.report.service.ReportService;
import com.gamegoo.gamegoo_v2.core.event.SendReportEmailEvent;
import com.gamegoo.gamegoo_v2.core.event.listener.EmailEventListener;
import com.gamegoo.gamegoo_v2.core.exception.ReportException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportTypeMappingRepository reportTypeMappingRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRecentStatsRepository memberRecentStatsRepository;

    @MockitoSpyBean
    private EmailEventListener emailEventListener;

    private Member fromMember;
    private Member toMember;
    private Board board;

    @BeforeEach
    void setUp() {
        fromMember = createMember("from@gmail.com", "fromMember");
        toMember = createMember("to@gmail.com", "toMember");
        board = createBoard(toMember);
    }

    @AfterEach
    void tearDown() {
        memberRecentStatsRepository.deleteAll();
        reportTypeMappingRepository.deleteAllInBatch();
        reportRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("신고 등록")
    class InsertReportTest {

        @DisplayName("실패: 자기 자신을 신고하는 경우")
        @Test
        void insertReportFailedWhenSelfReport() {
            // given
            List<Integer> reportCodes = List.of(1, 2, 3);
            String content = "신고 내용";
            Integer pathCode = 1;

            // when // then
            assertThatThrownBy(
                    () -> reportService.insertReport(fromMember, fromMember, reportCodes, content, pathCode, null))
                    .hasMessage(ErrorCode._BAD_REQUEST.getMessage());
        }

        @DisplayName("실패: 탈퇴한 회원을 신고하는 경우")
        @Test
        void insertReportFailedWhenTargetIsBlind() {
            // given
            toMember.updateBlind(true);
            memberRepository.save(toMember);

            List<Integer> reportCodes = List.of(1, 2, 3);
            String content = "신고 내용";
            Integer pathCode = 1;

            // when // then
            assertThatThrownBy(
                    () -> reportService.insertReport(fromMember, toMember, reportCodes, content, pathCode, null))
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("성공: 신고 등록 (게시글 없음)")
        @Test
        void insertReportSucceeds() {
            // given
            List<Integer> reportCodes = List.of(1, 2, 3);
            String content = "신고 내용";
            Integer pathCode = 1;

            // when
            Report result = reportService.insertReport(fromMember, toMember, reportCodes, content, pathCode, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFromMember().getId()).isEqualTo(fromMember.getId());
            assertThat(result.getToMember().getId()).isEqualTo(toMember.getId());
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getPath()).isEqualTo(ReportPath.of(pathCode));
            assertThat(result.getSourceBoard()).isNull();

            List<ReportTypeMapping> mappings = reportTypeMappingRepository.findAllByReportId(result.getId());
            assertThat(mappings).hasSize(3);
            mappings.forEach(mapping -> assertThat(mapping.getCode()).isIn(reportCodes));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailEventListener, times(1)).handleSendReportEmailEvent(any(SendReportEmailEvent.class));
            });
        }

        @DisplayName("성공: 신고 등록 (게시글 포함)")
        @Test
        void insertReportSucceedsWithBoard() {
            // given
            List<Integer> reportCodes = List.of(1, 2);
            String content = "신고 내용";
            Integer pathCode = 2;

            // when
            Report result = reportService.insertReport(fromMember, toMember, reportCodes, content, pathCode, board);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFromMember().getId()).isEqualTo(fromMember.getId());
            assertThat(result.getToMember().getId()).isEqualTo(toMember.getId());
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getPath()).isEqualTo(ReportPath.of(pathCode));
            assertThat(result.getSourceBoard().getId()).isEqualTo(board.getId());

            List<ReportTypeMapping> mappings = reportTypeMappingRepository.findAllByReportId(result.getId());
            assertThat(mappings).hasSize(2);
            mappings.forEach(mapping -> assertThat(mapping.getCode()).isIn(reportCodes));

            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailEventListener, times(1)).handleSendReportEmailEvent(any(SendReportEmailEvent.class));
            });
        }

        @DisplayName("성공: 신고 등록 (내용 없음)")
        @Test
        void insertReportSucceedsWithoutContent() {
            // given
            List<Integer> reportCodes = List.of(4, 5);
            Integer pathCode = 3;

            // when
            Report result = reportService.insertReport(fromMember, toMember, reportCodes, null, pathCode, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFromMember().getId()).isEqualTo(fromMember.getId());
            assertThat(result.getToMember().getId()).isEqualTo(toMember.getId());
            assertThat(result.getContent()).isNull();
            assertThat(result.getPath()).isEqualTo(ReportPath.of(pathCode));
            assertThat(result.getSourceBoard()).isNull();

            List<ReportTypeMapping> mappings = reportTypeMappingRepository.findAllByReportId(result.getId());
            assertThat(mappings).hasSize(2);
            mappings.forEach(mapping -> assertThat(mapping.getCode()).isIn(reportCodes));
        }

    }

    @Nested
    @DisplayName("신고 존재 여부 확인")
    class ExistsByMemberAndCreatedAtTest {

        @DisplayName("오늘 날짜에 신고가 존재하는 경우 true 반환")
        @Test
        void existsByMemberAndCreatedAtReturnsTrueWhenExists() {
            // given
            reportRepository.save(Report.create(fromMember, toMember, "content", ReportPath.CHAT, null));
            LocalDate today = LocalDate.now();

            // when
            boolean exists = reportService.existsByMemberAndCreatedAt(fromMember, toMember, today);

            // then
            assertThat(exists).isTrue();
        }

        @DisplayName("오늘 날짜에 신고가 존재하지 않는 경우 false 반환")
        @Test
        void existsByMemberAndCreatedAtReturnsFalseWhenNotExists() {
            // given
            LocalDate today = LocalDate.now();

            // when
            boolean exists = reportService.existsByMemberAndCreatedAt(fromMember, toMember, today);

            // then
            assertThat(exists).isFalse();
        }

        @DisplayName("다른 날짜에 신고가 존재하는 경우 false 반환")
        @Test
        void existsByMemberAndCreatedAtReturnsFalseWhenDifferentDate() {
            // given
            reportRepository.save(Report.create(fromMember, toMember, "content", ReportPath.CHAT, null));
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // when
            boolean exists = reportService.existsByMemberAndCreatedAt(fromMember, toMember, yesterday);

            // then
            assertThat(exists).isFalse();
        }

    }

    @Nested
    @DisplayName("신고 조회")
    class FindByIdTest {

        @DisplayName("성공: ID로 신고 조회")
        @Test
        void findByIdSucceeds() {
            // given
            Report savedReport = reportRepository.save(
                    Report.create(fromMember, toMember, "content", ReportPath.CHAT, null));

            // when
            Report result = reportService.findById(savedReport.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedReport.getId());
            assertThat(result.getFromMember().getId()).isEqualTo(fromMember.getId());
            assertThat(result.getToMember().getId()).isEqualTo(toMember.getId());
        }

        @DisplayName("실패: 존재하지 않는 ID로 조회")
        @Test
        void findByIdFailedWhenNotFound() {
            // when // then
            assertThatThrownBy(() -> reportService.findById(999L))
                    .isInstanceOf(ReportException.class)
                    .hasMessage(ErrorCode.REPORT_NOT_FOUND.getMessage());
        }

    }

    @Nested
    @DisplayName("신고 유형 문자열 조회")
    class GetReportTypeStringTest {

        @DisplayName("성공: 신고 유형 문자열 조회")
        @Test
        void getReportTypeStringSucceeds() {
            // given
            Report savedReport = reportRepository.save(
                    Report.create(fromMember, toMember, "content", ReportPath.CHAT, null));
            reportTypeMappingRepository.save(ReportTypeMapping.create(savedReport, 4));
            reportTypeMappingRepository.save(ReportTypeMapping.create(savedReport, 2));

            // when
            String result = reportService.getReportTypeString(savedReport.getId());

            // then
            assertThat(result).contains("욕설/ 혐오/ 차별적 표현");
            assertThat(result).contains("불법 정보 포함");
            assertThat(result).contains(", ");
        }

        @DisplayName("성공: 단일 신고 유형 문자열 조회")
        @Test
        void getReportTypeStringSucceedsWithSingleType() {
            // given
            Report savedReport = reportRepository.save(
                    Report.create(fromMember, toMember, "content", ReportPath.CHAT, null));
            reportTypeMappingRepository.save(ReportTypeMapping.create(savedReport, 4));

            // when
            String result = reportService.getReportTypeString(savedReport.getId());

            // then
            assertThat(result).isEqualTo("욕설/ 혐오/ 차별적 표현");
        }

    }

    @Nested
    @DisplayName("전체 신고 목록 조회")
    class GetAllReportsTest {

        @DisplayName("성공: 전체 신고 목록 조회")
        @Test
        void getAllReportsSucceeds() {
            // given
            Report report1 = reportRepository.save(
                    Report.create(fromMember, toMember, "content1", ReportPath.CHAT, null));
            Report report2 = reportRepository.save(
                    Report.create(toMember, fromMember, "content2", ReportPath.BOARD, board));

            // when
            List<Report> results = reportService.getAllReports();

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Report::getId)
                    .containsExactlyInAnyOrder(report1.getId(), report2.getId());
        }

        @DisplayName("성공: 신고가 없는 경우 빈 리스트 반환")
        @Test
        void getAllReportsSucceedsWhenEmpty() {
            // when
            List<Report> results = reportService.getAllReports();

            // then
            assertThat(results).isEmpty();
        }

    }

    private Member createMember(String email, String gameName) {
        Member member = Member.builder()
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
                .build();

        memberRecentStatsRepository.save(MemberRecentStats.builder()
                .member(member)
                .build());

        return memberRepository.save(member);
    }

    private Board createBoard(Member member) {
        return boardRepository.save(Board.builder()
                .member(member)
                .gameMode(GameMode.SOLO)
                .mainP(Position.ADC)
                .subP(Position.JUNGLE)
                .wantP(List.of(Position.ADC))
                .mike(Mike.AVAILABLE)
                .content("content")
                .boardProfileImage(1)
                .build());
    }

}
