package com.gamegoo.gamegoo_v2.content.report.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportListResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportProcessResponse;
import com.gamegoo.gamegoo_v2.core.exception.ReportException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.account.member.service.BanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFacadeService {

    private final ReportService reportService;
    private final MemberService memberService;
    private final BoardService boardService;
    private final BanService banService;

    /**
     * 신고 등록 facade 메소드
     *
     * @param member         회원
     * @param targetMemberId 대상 회원 id
     * @param request        신고 등록 요청
     * @return ReportInsertResponse
     */
    @Transactional
    public ReportInsertResponse addReport(Member member, Long targetMemberId, ReportRequest request) {
        Member targetMember = memberService.findMemberById(targetMemberId);

        // 오늘 날짜에 해당 회원에게 신고 내역이 존재하는 경우 쿨타임 적용
        boolean exists = reportService.existsByMemberAndCreatedAt(member, targetMember, LocalDate.now());

        if (exists) {
            throw new ReportException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Board board = (request.getBoardId() != null) ? boardService.findBoard(request.getBoardId()) : null;

        Report report = reportService.insertReport(member, targetMember, request.getReportCodeList(),
                request.getContents(), request.getPathCode(), board);

        return ReportInsertResponse.of(report);
    }


    /**
     * 신고 목록 고급 필터링 조회 (관리자용)
     */
    public List<ReportListResponse> searchReports(ReportSearchRequest request) {
        return reportService.searchReports(request).stream()
                .map(report -> ReportListResponse.builder()
                        .reportId(report.getId())
                        .fromMemberName(report.getFromMember().getGameName())
                        .toMemberName(report.getToMember().getGameName())
                        .content(report.getContent())
                        .reportType(reportService.getReportTypeString(report.getId()))
                        .path(report.getPath().name())
                        .createdAt(report.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
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
     * @return 삭제 성공 여부
     */
    @Transactional
    public boolean deleteReportedPost(Long reportId) {
        Report report = reportService.findById(reportId);
        
        if (report.getSourceBoard() != null) {
            Board board = report.getSourceBoard();
            boardService.deleteBoard(board.getId(), board.getMember().getId());
            return true;
        }
        
        return false;
    }

}
