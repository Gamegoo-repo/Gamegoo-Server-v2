package com.gamegoo.gamegoo_v2.content.report.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.report.domain.Report;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportTypeMapping;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportRepository;
import com.gamegoo.gamegoo_v2.content.report.repository.ReportTypeMappingRepository;
import com.gamegoo.gamegoo_v2.core.common.validator.MemberValidator;
import com.gamegoo.gamegoo_v2.core.exception.ReportException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final MemberValidator memberValidator;
    private final ReportRepository reportRepository;
    private final ReportTypeMappingRepository reportTypeMappingRepository;

    /**
     * 대상 회원에 대한 신고 엔티티 생성 및 저장
     *
     * @param member       회원
     * @param targetMember 대상 회원
     * @param reportCodes  신고 유형 코드 list
     * @param content      신고 텍스트
     * @param board        관련 게시글
     * @return Report
     */
    @Transactional
    public Report insertReport(Member member, Member targetMember, List<Integer> reportCodes, String content,
                               Board board) {
        // code 값 검증
        validateReportTypeIds(reportCodes);

        // targetMember로 나 자신을 요청한 경우 검증
        memberValidator.throwIfEqual(member, targetMember);

        // 상대방의 탈퇴 여부 검증
        memberValidator.throwIfBlind(targetMember);

        // report 엔티티 생성 및 저장
        Report report = reportRepository.save(Report.create(member, targetMember, content, board));

        // reportTypeMapping 엔티티 생성 및 저장
        List<ReportTypeMapping> reportTypeMappings = reportCodes.stream()
                .map(code -> ReportTypeMapping.create(report, code))
                .toList();
        reportTypeMappingRepository.saveAll(reportTypeMappings);

        return report;
    }

    /**
     * 신고 유형 코드 검증
     *
     * @param reportCodes 신고 유형 코드 list
     */
    private void validateReportTypeIds(List<Integer> reportCodes) {
        for (Integer code : reportCodes) {
            if (code == null || code < 1 || code > 6) {
                throw new ReportException(ErrorCode.REPORT_CODE_BAD_REQUEST);
            }
        }
    }

}
