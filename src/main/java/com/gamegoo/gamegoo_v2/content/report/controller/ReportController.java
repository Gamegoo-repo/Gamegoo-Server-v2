package com.gamegoo.gamegoo_v2.content.report.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.content.report.dto.request.ReportRequest;
import com.gamegoo.gamegoo_v2.content.report.dto.response.ReportInsertResponse;
import com.gamegoo.gamegoo_v2.content.report.service.ReportFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reposrt", description = "Report 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class ReportController {

    private final ReportFacadeService reportFacadeService;

    @Operation(summary = "신고 등록 API", description = "대상 회원에 대한 신고를 등록하는 API 입니다.")
    @Parameter(name = "memberId", description = "신고할 대상 회원의 id 입니다.")
    @GetMapping("/{memberId}")
    public ApiResponse<ReportInsertResponse> addReport(@PathVariable("memberId") Long targetMemberId,
                                                       @Valid @RequestBody ReportRequest request,
                                                       @AuthMember Member member) {
        return ApiResponse.ok(reportFacadeService.addReport(member, targetMemberId, request));
    }

}
