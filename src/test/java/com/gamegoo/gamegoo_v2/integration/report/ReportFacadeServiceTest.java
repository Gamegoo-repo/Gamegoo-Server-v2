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
import com.gamegoo.gamegoo_v2.notification.domain.Notification;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationType;
import com.gamegoo.gamegoo_v2.notification.domain.NotificationTypeTitle;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationRepository;
import com.gamegoo.gamegoo_v2.notification.repository.NotificationTypeRepository;
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

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

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
        notificationRepository.deleteAllInBatch();
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

    @Nested
    @DisplayName("신고 처리")
    class ProcessReportTest {

        @DisplayName("성공: 신고 처리로 제재를 적용한다")
        @Test
        @org.springframework.transaction.annotation.Transactional
        void processReport_Success() {
            // given
            Report report = createReport();
            com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest request =
                    com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest.builder()
                            .banType(com.gamegoo.gamegoo_v2.account.member.domain.BanType.BAN_1D)
                            .processReason("부적절한 내용")
                            .build();

            // when
            com.gamegoo.gamegoo_v2.content.report.dto.response.ReportProcessResponse response =
                    reportFacadeService.processReport(report.getId(), request);

            // then
            assertThat(response.getReportId()).isEqualTo(report.getId());
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getAppliedBanType()).isEqualTo(com.gamegoo.gamegoo_v2.account.member.domain.BanType.BAN_1D);
            assertThat(response.getBanExpireAt()).isNotNull();
            assertThat(response.getMessage()).isEqualTo("신고 처리가 완료되었습니다.");

            // 회원 제재 상태 확인
            Member updatedMember = memberRepository.findById(targetMember.getId()).orElseThrow();
            assertThat(updatedMember.getBanType()).isEqualTo(com.gamegoo.gamegoo_v2.account.member.domain.BanType.BAN_1D);
            assertThat(updatedMember.getBanExpireAt()).isNotNull();

            // 알림 생성 확인
            List<Notification> allNotifications = notificationRepository.findAll();

            List<Notification> reporterNotifications = allNotifications.stream()
                    .filter(n -> n.getMember().getId().equals(member.getId()))
                    .toList();
            assertThat(reporterNotifications).hasSize(1);
            assertThat(reporterNotifications.get(0).getNotificationType().getTitle())
                    .isEqualTo(NotificationTypeTitle.REPORT_PROCESSED_REPORTER);

            List<Notification> reportedNotifications = allNotifications.stream()
                    .filter(n -> n.getMember().getId().equals(targetMember.getId()))
                    .toList();
            assertThat(reportedNotifications).hasSize(1);
            assertThat(reportedNotifications.get(0).getNotificationType().getTitle())
                    .isEqualTo(NotificationTypeTitle.REPORT_PROCESSED_REPORTED);
        }

        @DisplayName("성공: 신고 처리로 제재 없음(NONE)을 적용한다 - 신고당한 자에게는 알림 미전송")
        @Test
        @org.springframework.transaction.annotation.Transactional
        void processReport_SuccessWithNoBan() {
            // given
            Report report = createReport();
            com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest request =
                    com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest.builder()
                            .banType(com.gamegoo.gamegoo_v2.account.member.domain.BanType.NONE)
                            .processReason("신고 내용이 부적절하지 않음")
                            .build();

            // when
            com.gamegoo.gamegoo_v2.content.report.dto.response.ReportProcessResponse response =
                    reportFacadeService.processReport(report.getId(), request);

            // then
            assertThat(response.getReportId()).isEqualTo(report.getId());
            assertThat(response.getTargetMemberId()).isEqualTo(targetMember.getId());
            assertThat(response.getAppliedBanType()).isEqualTo(com.gamegoo.gamegoo_v2.account.member.domain.BanType.NONE);

            // 신고자에게만 알림 전송 확인
            List<Notification> allNotifications = notificationRepository.findAll();

            List<Notification> reporterNotifications = allNotifications.stream()
                    .filter(n -> n.getMember().getId().equals(member.getId()))
                    .toList();
            assertThat(reporterNotifications).hasSize(1);
            assertThat(reporterNotifications.get(0).getNotificationType().getTitle())
                    .isEqualTo(NotificationTypeTitle.REPORT_PROCESSED_REPORTER);

            // 신고당한 자에게는 알림 미전송 확인
            List<Notification> reportedNotifications = allNotifications.stream()
                    .filter(n -> n.getMember().getId().equals(targetMember.getId()))
                    .toList();
            assertThat(reportedNotifications).hasSize(0);
        }

        @DisplayName("실패: 존재하지 않는 신고 ID")
        @Test
        void processReport_NotFoundReport() {
            // given
            com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest request =
                    com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest.builder()
                            .banType(com.gamegoo.gamegoo_v2.account.member.domain.BanType.BAN_1D)
                            .processReason("부적절한 내용")
                            .build();

            // when // then
            assertThatThrownBy(() -> reportFacadeService.processReport(999L, request))
                    .isInstanceOf(ReportException.class)
                    .hasMessage(ErrorCode.REPORT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("신고된 게시글 삭제")
    class DeleteReportedPostTest {

        @DisplayName("성공: 신고된 게시글 삭제")
        @Test
        void deleteReportedPost_Success() {
            // given
            Board board = createBoard(targetMember);
            Report report = createReportWithBoard(board);

            // when
            String result = reportFacadeService.deleteReportedPost(report.getId());

            // then
            assertThat(result).isEqualTo("신고된 게시글 삭제가 완료되었습니다");
            Board deletedBoard = boardRepository.findById(board.getId()).orElseThrow();
            assertThat(deletedBoard.isDeleted()).isTrue();
        }

        @DisplayName("성공: 게시글이 없는 신고의 경우 false 반환")
        @Test
        void deleteReportedPost_NoBoard() {
            // given
            Report report = createReport();

            // when
            String result = reportFacadeService.deleteReportedPost(report.getId());

            // then
            assertThat(result).isEqualTo("삭제할 게시글이 존재하지 않습니다");
        }

        @DisplayName("실패: 존재하지 않는 신고 ID")
        @Test
        void deleteReportedPost_NotFoundReport() {
            // when // then
            assertThatThrownBy(() -> reportFacadeService.deleteReportedPost(999L))
                    .isInstanceOf(ReportException.class)
                    .hasMessage(ErrorCode.REPORT_NOT_FOUND.getMessage());
        }
    }

    private Report createReport() {
        Report report = reportRepository.save(Report.create(
                member,
                targetMember,
                "신고 내용",
                ReportPath.PROFILE,
                null
        ));

        // ReportTypeMapping 추가 (테스트용 신고 유형)
        reportTypeMappingRepository.save(ReportTypeMapping.create(report, 1)); // SPAM
        reportTypeMappingRepository.save(ReportTypeMapping.create(report, 4)); // HATE_SPEECH

        return report;
    }

    private Report createReportWithBoard(Board board) {
        return reportRepository.save(Report.create(
                member,
                targetMember,
                "신고 내용",
                ReportPath.BOARD,
                board
        ));
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
