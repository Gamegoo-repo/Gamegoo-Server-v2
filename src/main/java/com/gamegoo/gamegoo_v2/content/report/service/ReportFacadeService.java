package com.gamegoo.gamegoo_v2.content.report.service;

import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportListResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportPageResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportProcessResponse;
import com.gamegoo.gamegoo_v2.core.exception.ReportException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFacadeService {

    private final ReportService reportService;
    private final MemberService memberService;
    private final BoardService boardService;
    private final BanService banService;
    private final NotificationService notificationService;

    /**
     * 신고 등록 facade 메소드
     *
     * @param member         회원 (비회원 요청의 경우 null)
     * @param targetMemberId 대상 회원 id
     * @param request        신고 등록 요청
     * @return ReportInsertResponse
     */
    @Transactional
    public ReportInsertResponse addReport(Member member, Long targetMemberId, ReportRequest request) {
        Member targetMember = memberService.findMemberById(targetMemberId);

        // 오늘 날짜에 해당 회원에게 신고 내역이 존재하는 경우 쿨타임 적용
        if (member != null) { // 쿨타임 로직은 회원만 해당
            boolean exists = reportService.existsByMemberAndCreatedAt(member, targetMember, LocalDate.now());
            if (exists) {
                throw new ReportException(ErrorCode.REPORT_ALREADY_EXISTS);
            }
        }

        Board board = (request.getBoardId() != null) ? boardService.findBoard(request.getBoardId()) : null;

        Report report = reportService.insertReport(member, targetMember, request.getReportCodeList(),
                request.getContents(), request.getPathCode(), board);

        return ReportInsertResponse.of(report);
    }


    /**
     * 신고 목록 고급 필터링 조회 (관리자용)
     */
    public ReportPageResponse searchReports(ReportSearchRequest request,
                                            org.springframework.data.domain.Pageable pageable) {
        Page<Report> reportPage = reportService.searchReports(request, pageable);

        // 각 신고에 대해 reportCount를 조회하여 ReportListResponse 생성
        List<ReportListResponse> reportList = reportPage.getContent().stream()
                .map(report -> {
                    Long reportCount = reportService.getReportCountByMemberId(report.getToMember().getId());
                    return ReportListResponse.of(report, reportCount);
                })
                .toList();

        int totalPage = (reportPage.getTotalPages() == 0) ? 1 : reportPage.getTotalPages();
        return ReportPageResponse.builder()
                .reports(reportList)
                .totalPages(totalPage)
                .totalElements(reportPage.getTotalElements())
                .currentPage(reportPage.getNumber())
                .build();
    }

    /**
     * 관리자 신고 처리 facade 메소드
     *
     * @param reportId 신고 ID
     * @param request  신고 처리 요청
     * @return ReportProcessResponse
     */
    @Transactional
    public ReportProcessResponse processReport(Long reportId, ReportProcessRequest request) {
        Report report = reportService.findById(reportId);
        Member targetMember = report.getToMember();

        // 제재 적용
        banService.applyBan(targetMember, request.getBanType());

        // 신고 처리 결과 알림 생성
        String reportTypeString = reportService.getReportTypeString(reportId);
        String banDescription = banService.getBanReasonMessage(request.getBanType());

        // 신고자에게 알림 전송
        Member reporter = report.getFromMember();
        if (reporter != null) {
            notificationService.createReportProcessedNotificationForReporter(
                    reporter, reportTypeString, banDescription
            );
        }

        // 신고 당한 회원에게 알림 전송 (제재가 있는 경우에만)
        if (request.getBanType() != null && request.getBanType() != BanType.NONE) {
            notificationService.createReportProcessedNotificationForReported(
                    targetMember, reportTypeString, banDescription
            );
        }

        return ReportProcessResponse.of(
                reportId,
                targetMember.getId(),
                request.getBanType(),
                targetMember.getBanExpireAt()
        );
    }

    /**
     * 신고된 게시글 삭제 facade 메소드
     *
     * @param reportId 신고 ID
     * @return 삭제 결과 메시지
     */
    @Transactional
    public String deleteReportedPost(Long reportId) {
        Report report = reportService.findById(reportId);

        if (report.getSourceBoard() != null) {
            Board board = report.getSourceBoard();
            boardService.deleteBoard(board.getId(), board.getMember().getId());
            return "신고된 게시글 삭제가 완료되었습니다";
        }

        return "삭제할 게시글이 존재하지 않습니다";
    }

}
