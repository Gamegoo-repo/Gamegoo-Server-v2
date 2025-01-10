package com.gamegoo.gamegoo_v2.content.report.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.service.BoardService;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFacadeService {

    private final ReportService reportService;
    private final MemberService memberService;
    private final BoardService boardService;

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

        Board board = (request.getBoardId() != null) ? boardService.findBoard(request.getBoardId()) : null;

        Report report = reportService.insertReport(member, targetMember, request.getReportCodeList(),
                request.getContents(), board);

        return ReportInsertResponse.of(report);
    }

}
