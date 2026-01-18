package com.gamegoo.gamegoo_v2.content.report.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.BanType;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportPath;
import com.gamegoo.gamegoo_v2.content.report.domain.ReportSortOrder;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportProcessRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportSearchRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.BanReleaseResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportPageResponse;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportProcessResponse;
import com.gamegoo.gamegoo_v2.content.report.service.ReportFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.config.swagger.ApiErrorCodes;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Report", description = "Report 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/report")
public class ReportController {

    private final ReportFacadeService reportFacadeService;

    @Operation(summary = "신고 등록 API", description = "대상 회원에 대한 신고를 등록하는 API 입니다.")
    @Parameter(name = "memberId", description = "신고할 대상 회원의 id 입니다.")
    @PostMapping("/{memberId}")
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode._BAD_REQUEST,
            ErrorCode.TARGET_MEMBER_DEACTIVATED,
            ErrorCode.REPORT_ALREADY_EXISTS,
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.REPORT_PATH_NOT_FOUND
    })
    public ApiResponse<ReportInsertResponse> addReport(@PathVariable("memberId") Long targetMemberId,
                                                       @Valid @RequestBody ReportRequest request,
                                                       @AuthMember(required = false) Member member) {
        return ApiResponse.ok(reportFacadeService.addReport(member, targetMemberId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "신고 목록 조회 (관리자 전용)",
            description = """
                    관리자만 접근 가능한 신고 목록 고급 필터링 조회 API입니다.

                    **필터링 옵션:**
                    - reportedMemberKeyword: 피신고자 검색 (게임명, 태그, 게임명#태그 형식 지원)
                    - reporterKeyword: 신고자 검색 (게임명, 태그, 게임명#태그 형식 지원)
                    - contentKeyword: 신고 내용으로 검색
                    - reportPaths: 신고 경로 (BOARD=게시판, CHAT=채팅, PROFILE=프로필)
                    - reportTypes: 신고 사유 (1=스팸, 2=불법정보, 3=성희롱, 4=욕설/혐오, 5=개인정보노출, 6=불쾌한표현)
                    - startDate/endDate: 신고 날짜 범위 (yyyy-MM-dd'T'HH:mm:ss)
                    - reportCountMin/Max/Exact: 누적 신고 횟수 필터
                    - isDeleted: 게시물 삭제 여부 (true/false)
                    - banTypes: 제재 상태 (NONE, WARNING, BAN_1D, BAN_3D, BAN_5D, BAN_1W, BAN_2W, BAN_1M, PERMANENT)
                    - sortOrder: 정렬 순서 (LATEST: 최신순, OLDEST: 오래된순) - 기본값: LATEST
                    - page/size: 페이징 (예: page=0&size=10)

                    **사용 예시:**
                    /api/v2/report/list?reportedMemberKeyword=홍길동#KR1&reportTypes=1,4&startDate=2024-01-01T00:00:00&banTypes=WARNING&page=0&size=10
                    """)
    @GetMapping("/list")
    @ApiErrorCodes({
            ErrorCode._FORBIDDEN,
            ErrorCode._BAD_REQUEST
    })
    public ApiResponse<ReportPageResponse> getReportList(
            @RequestParam(required = false) String reportedMemberKeyword,
            @RequestParam(required = false) String reporterKeyword,
            @RequestParam(required = false) String contentKeyword,
            @Parameter(description = "신고 경로", schema = @Schema(ref = "#/components/schemas/ReportPath"))
            @RequestParam(required = false) List<ReportPath> reportPaths,
            @Parameter(description = "신고 유형 (1=스팸, 2=불법정보, 3=성희롱, 4=욕설/혐오, 5=개인정보노출, 6=불쾌한표현)", schema = @Schema(ref = "#/components/schemas/ReportType"))
            @RequestParam(required = false) List<Integer> reportTypes,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Integer reportCountMin,
            @RequestParam(required = false) Integer reportCountMax,
            @RequestParam(required = false) Integer reportCountExact,
            @RequestParam(required = false) Boolean isDeleted,
            @Parameter(description = "제재 상태", schema = @Schema(ref = "#/components/schemas/BanType"))
            @RequestParam(required = false) List<BanType> banTypes,
            @Parameter(description = "정렬 순서", schema = @Schema(ref = "#/components/schemas/ReportSortOrder"))
            @RequestParam(required = false) ReportSortOrder sortOrder,
            Pageable pageable) {

        ReportSearchRequest request = ReportSearchRequest.builder()
                .reportedMemberKeyword(reportedMemberKeyword)
                .reporterKeyword(reporterKeyword)
                .contentKeyword(contentKeyword)
                .reportPaths(reportPaths)
                .reportTypes(reportTypes)
                .startDate(startDate)
                .endDate(endDate)
                .reportCountMin(reportCountMin)
                .reportCountMax(reportCountMax)
                .reportCountExact(reportCountExact)
                .isDeleted(isDeleted)
                .banTypes(banTypes)
                .sortOrder(sortOrder)
                .build();

        return ApiResponse.ok(reportFacadeService.searchReports(request, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "신고 처리 (관리자 전용)",
            description = """
                    관리자가 신고를 처리하여 제재를 적용하는 API입니다.

                    **Request Body:**
                    - banType: 적용할 제재 유형 (필수)
                      - NONE: 제재 없음
                      - WARNING: 경고
                      - BAN_1D: 1일 정지
                      - BAN_3D: 3일 정지
                      - BAN_5D: 5일 정지
                      - BAN_1W: 1주 정지
                      - BAN_2W: 2주 정지
                      - BAN_1M: 1개월 정지
                      - PERMANENT: 영구 정지
                    - processReason: 제재 사유 (선택사항)
                    """)
    @Parameter(name = "reportId", description = "처리할 신고의 ID입니다.")
    @PutMapping("/{reportId}/process")
    @ApiErrorCodes({ErrorCode.REPORT_NOT_FOUND})
    public ApiResponse<ReportProcessResponse> processReport(@PathVariable("reportId") Long reportId,
                                                            @Valid @RequestBody ReportProcessRequest request) {
        return ApiResponse.ok(reportFacadeService.processReport(reportId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "신고된 게시글 삭제 (관리자 전용)",
            description = """
                    관리자가 신고된 게시글을 삭제하는 API입니다.

                    해당 신고와 연관된 게시글이 있는 경우 삭제 처리되며,
                    게시글이 없는 경우 적절한 메시지가 반환됩니다.

                    **반환 메시지:**
                    - 성공: "신고된 게시글 삭제가 완료되었습니다"
                    - 게시글 없음: "삭제할 게시글이 존재하지 않습니다"
                    """)
    @Parameter(name = "reportId", description = "삭제할 게시글과 연관된 신고의 ID입니다.")
    @DeleteMapping("/{reportId}/post")
    @ApiErrorCodes({
            ErrorCode.REPORT_NOT_FOUND,
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.DELETE_BOARD_ACCESS_DENIED
    })
    public ApiResponse<String> deleteReportedPost(@PathVariable("reportId") Long reportId) {
        return ApiResponse.ok(reportFacadeService.deleteReportedPost(reportId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "회원 정지 해제 (관리자 전용)",
            description = """
                    관리자가 회원의 정지를 해제하는 API입니다.

                    정지 상태인 회원의 banType을 NONE으로 변경하고,
                    banExpireAt을 null로 초기화합니다.

                    **Response:**
                    - memberId: 정지 해제된 회원 ID
                    - gameName: 회원의 게임 이름
                    - tag: 회원의 태그
                    - previousBanType: 해제 전 정지 유형
                    - message: "정지가 해제되었습니다."
                    """)
    @Parameter(name = "memberId", description = "정지 해제할 회원의 ID입니다.")
    @PutMapping("/member/{memberId}/unban")
    @ApiErrorCodes({
            ErrorCode._FORBIDDEN,
            ErrorCode.MEMBER_NOT_FOUND
    })
    public ApiResponse<BanReleaseResponse> releaseMemberBan(@PathVariable("memberId") Long memberId) {
        return ApiResponse.ok(reportFacadeService.releaseMemberBan(memberId));
    }

}
