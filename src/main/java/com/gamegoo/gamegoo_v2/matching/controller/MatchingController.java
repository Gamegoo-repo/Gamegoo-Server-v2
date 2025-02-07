package com.gamegoo.gamegoo_v2.matching.controller;

import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.matching.dto.request.InitializingMatchingRequest;
import com.gamegoo.gamegoo_v2.matching.dto.request.ModifyMatchingStatusRequest;
import com.gamegoo.gamegoo_v2.matching.dto.response.PriorityListResponse;
import com.gamegoo.gamegoo_v2.matching.service.MatchingFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Matching", description = "매칭 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/internal")
@Validated
public class MatchingController {

    private final MatchingFacadeService matchingFacadeService;

    @Operation(summary = "매칭 우선순위 계산 및 기록 저장 API", description = "API for calculating and recording matching")
    @PostMapping("/matching/priority/{memberId}")
    public ApiResponse<PriorityListResponse> InitializeMatching(@PathVariable(name = "memberId") Long memberId,
                                                                @RequestBody @Valid InitializingMatchingRequest request) {
        return ApiResponse.ok(matchingFacadeService.calculatePriorityAndRecording(memberId, request));
    }

    @Operation(summary = "내 매칭 status 변경", description = "API for updating my matching status")
    @PatchMapping("/matching/status")
    public ApiResponse<String> UpdateMatchingStatus(@RequestBody @Valid ModifyMatchingStatusRequest request) {
        return ApiResponse.ok(matchingFacadeService.modifyMyMatchingStatus(request));
    }

    @Operation(summary = "나와 상대방 매칭 status 변경", description = "API for updating both matching status")
    @PatchMapping("/matching/status/target")
    public ApiResponse<String> UpdateBothMatchingStatus(@RequestBody @Valid ModifyMatchingStatusRequest request) {
        return ApiResponse.ok(matchingFacadeService.modifyBothMatchingStatus(request));
    }

}
