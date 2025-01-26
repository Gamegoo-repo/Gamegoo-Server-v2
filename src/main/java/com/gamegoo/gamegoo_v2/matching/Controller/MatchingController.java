package com.gamegoo.gamegoo_v2.matching.Controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.matching.dto.request.InitializingMatchingRequest;
import com.gamegoo.gamegoo_v2.matching.dto.response.PriorityListResponse;
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

    @Operation(summary = "매칭 우선순위 계산 및 기록 저장 API", description = """
            API for calculating and recording matching
            gameMode: FAST, SOLO, FREE, ARAM string 을 넣어주세요.\s
            mike: UNAVAILABLE 또는 AVAILABLE 를 넣어주세요.\s
            matchingType: "BASIC" 또는 "PRECISE"를 넣어주세요. \s
            mainP: ANY, TOP, JUNGLE, MID, ADC, SUP string 을 넣어주세요.\s
            subP: ANY, TOP, JUNGLE, MID, ADC, SUP string 을 넣어주세요.\s
            wantP: ANY, TOP, JUNGLE, MID, ADC, SUP string 을 넣어주세요.\s
            gameStyleList: 1 ~ 17 int 를 넣어주세요.""")
    @GetMapping
    public ApiResponse<PriorityListResponse> InitializeMatching(@AuthMember Member member,
                                                                InitializingMatchingRequest request) {
        return ApiResponse.ok(matchingFacadeService.calculatePriorityAndRecording(member, request));
    }

}
