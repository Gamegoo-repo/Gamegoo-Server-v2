package com.gamegoo.gamegoo_v2.integration.report;

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
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportRepository;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportTypeMappingRepository;
import com.gamegoo.gamegoo_v2.content.report.service.ReportFacadeService;
import com.gamegoo.gamegoo_v2.core.event.SendReportEmailEvent;
import com.gamegoo.gamegoo_v2.core.event.listener.EmailEventListener;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.MemberException;
import com.gamegoo.gamegoo_v2.core.exception.ReportException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.core.exception.common.GlobalException;
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
class ReportFacadeServiceTest {

    @Autowired
    private ReportFacadeService reportFacadeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportTypeMappingRepository reportTypeMappingRepository;

    @Autowired
    private BoardRepository boardRepository;

    @MockitoSpyBean
    private EmailEventListener emailEventListener;

    private Member member;
    private Member targetMember;

    @BeforeEach
    void setUp() {
        member = createMember("test@gmail.com", "member");
        targetMember = createMember("target@gmail.com", "targetMember");
    }

    @AfterEach
    void tearDown() {
        reportTypeMappingRepository.deleteAllInBatch();
        reportRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("회원 신고 등록")
    class AddReportTest {

        @DisplayName("실패: 대상 회원을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void addReport_shouldThrownWhenMemberNotFound() {
            // given
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(List.of(1, 2, 3))
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when // then
            assertThatThrownBy(() -> reportFacadeService.addReport(member, 1000L, request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: boardId에 해당하는 게시글을 찾을 수 없는 경우 예외가 발생한다.")
        @Test
        void addReport_shouldThrownWhenBoardBotFound() {
            // given
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(List.of(1, 2, 3))
                    .contents("contents")
                    .pathCode(1)
                    .boardId(100L)
                    .build();

            // when // then
            assertThatThrownBy(() -> reportFacadeService.addReport(member, targetMember.getId(), request))
                    .isInstanceOf(BoardException.class)
                    .hasMessage(ErrorCode.BOARD_NOT_FOUND.getMessage());
        }

        @DisplayName("실패: 신고 대상으로 본인 id를 입력한 경우 예외가 발생한다.")
        @Test
        void addReport_shouldThrownWhenTargetIsSelf() {
            // given
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(List.of(1, 2, 3))
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when // then
            assertThatThrownBy(() -> reportFacadeService.addReport(member, member.getId(), request))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(ErrorCode._BAD_REQUEST.getMessage());
        }

        @DisplayName("실패: 대상 회원이 탈퇴한 경우 예외가 발생한다.")
        @Test
        void addReport_shouldThrownWhenTargetIsBlind() {
            // given
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(List.of(1, 2, 3))
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            targetMember.updateBlind(true);
            memberRepository.save(targetMember);

            // when // then
            assertThatThrownBy(() -> reportFacadeService.addReport(member, targetMember.getId(), request))
                    .isInstanceOf(MemberException.class)
                    .hasMessage(ErrorCode.TARGET_MEMBER_DEACTIVATED.getMessage());
        }

        @DisplayName("실패: 대상 회원에게 오늘 날짜로 등록된 신고 내역이 이미 존재하는 경우 쿨타임이 적용된다.")
        @Test
        void addReport_shouldThrownWhenCoolTime() {
            // given
            List<Integer> reportCodes = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodes)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            reportRepository.save(Report.create(member, targetMember, "content", ReportPath.CHAT, null));

            // when // then
            assertThatThrownBy(() -> reportFacadeService.addReport(member, targetMember.getId(), request))
                    .isInstanceOf(ReportException.class)
                    .hasMessage(ErrorCode.REPORT_ALREADY_EXISTS.getMessage());

        }

        @DisplayName("성공: boardId가 없는 경우")
        @Test
        void addReportSucceeds() {
            // given
            List<Integer> reportCodes = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodes)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when
            ReportInsertResponse response = reportFacadeService.addReport(member, targetMember.getId(), request);

            // then
            assertThat(response.getMessage()).isEqualTo("신고가 정상적으로 접수 되었습니다.");

            // report 엔티티 검증
            Report report = reportRepository.findById(response.getReportId()).orElseThrow();
            assertThat(report.getFromMember().getId()).isEqualTo(member.getId());
            assertThat(report.getToMember().getId()).isEqualTo(targetMember.getId());
            assertThat(report.getContent()).isEqualTo(request.getContents());
            assertThat(report.getPath()).isEqualTo(ReportPath.of(request.getPathCode()));
            assertThat(report.getSourceBoard()).isNull();

            // reportTypeMapping 엔티티 검증
            List<ReportTypeMapping> reportTypeMappings = reportTypeMappingRepository.findAllByReportId(report.getId());
            assertThat(reportTypeMappings.size()).isEqualTo(3);
            reportTypeMappings.forEach(rtm -> {
                assertThat(rtm.getCode()).isIn(reportCodes);
            });

            // event 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailEventListener, times(1)).handleSendReportEmailEvent(any(SendReportEmailEvent.class));
            });
        }

        @DisplayName("성공: boardId가 있는 경우")
        @Test
        void addReportSucceedsWithBoardId() {
            // given
            Board board = createBoard(targetMember);

            List<Integer> reportCodes = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodes)
                    .contents("contents")
                    .pathCode(1)
                    .boardId(board.getId())
                    .build();

            // when
            ReportInsertResponse response = reportFacadeService.addReport(member, targetMember.getId(), request);

            // then
            assertThat(response.getMessage()).isEqualTo("신고가 정상적으로 접수 되었습니다.");

            // report 엔티티 검증
            Report report = reportRepository.findById(response.getReportId()).orElseThrow();
            assertThat(report.getFromMember().getId()).isEqualTo(member.getId());
            assertThat(report.getToMember().getId()).isEqualTo(targetMember.getId());
            assertThat(report.getContent()).isEqualTo(request.getContents());
            assertThat(report.getPath()).isEqualTo(ReportPath.of(request.getPathCode()));
            assertThat(report.getSourceBoard().getId()).isEqualTo(board.getId());

            // reportTypeMapping 엔티티 검증
            List<ReportTypeMapping> reportTypeMappings = reportTypeMappingRepository.findAllByReportId(report.getId());
            assertThat(reportTypeMappings.size()).isEqualTo(3);
            reportTypeMappings.forEach(rtm -> {
                assertThat(rtm.getCode()).isIn(reportCodes);
            });

            // event 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailEventListener, times(1)).handleSendReportEmailEvent(any(SendReportEmailEvent.class));
            });
        }

        @DisplayName("성공: content 텍스트가 없는 경우")
        @Test
        void addReportSucceedsWithOutContents() {
            // given
            List<Integer> reportCodes = List.of(1, 2, 3);
            ReportRequest request = ReportRequest.builder()
                    .reportCodeList(reportCodes)
                    .contents(null)
                    .pathCode(1)
                    .boardId(null)
                    .build();

            // when
            ReportInsertResponse response = reportFacadeService.addReport(member, targetMember.getId(), request);

            // then
            assertThat(response.getMessage()).isEqualTo("신고가 정상적으로 접수 되었습니다.");

            // report 엔티티 검증
            Report report = reportRepository.findById(response.getReportId()).orElseThrow();
            assertThat(report.getFromMember().getId()).isEqualTo(member.getId());
            assertThat(report.getToMember().getId()).isEqualTo(targetMember.getId());
            assertThat(report.getContent()).isNull();
            assertThat(report.getPath()).isEqualTo(ReportPath.of(request.getPathCode()));
            assertThat(report.getSourceBoard()).isNull();

            // reportTypeMapping 엔티티 검증
            List<ReportTypeMapping> reportTypeMappings = reportTypeMappingRepository.findAllByReportId(report.getId());
            assertThat(reportTypeMappings.size()).isEqualTo(3);
            reportTypeMappings.forEach(rtm -> {
                assertThat(rtm.getCode()).isIn(reportCodes);
            });

            // event 검증
            await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
                verify(emailEventListener, times(1)).handleSendReportEmailEvent(any(SendReportEmailEvent.class));
            });
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
