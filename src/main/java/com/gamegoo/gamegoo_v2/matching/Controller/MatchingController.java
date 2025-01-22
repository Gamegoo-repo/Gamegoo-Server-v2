package com.gamegoo.gamegoo_v2.matching.Controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.matching.service.MatchingFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Matching", description = "매칭 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/internal/matching")
@Validated
public class MatchingController {

    private final MatchingFacadeService matchingFacadeService;

    @Operation(summary = "매칭 우선순위 계산 및 기록 저장 API", description = "API for calculating priority score and recording " +
            "matchign")
    @GetMapping
    public ApiResponse<String> getMemberJWT(@AuthMember Member member) {
        return ApiResponse.ok(matchingFacadeService.calculatePriorityAndRecording());
    }

}
