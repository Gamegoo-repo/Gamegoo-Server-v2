package com.gamegoo.gamegoo_v2.social.manner.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerInsertRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerInsertResponse;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Manner", description = "매너 평가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/manner")
public class MannerController {

    private final MannerFacadeService mannerFacadeService;

    @Operation(summary = "매너 평가 등록 API", description = "매너 평가를 등록하는 API 입니다.")
    @Parameter(name = "memberId", description = "매너 평가를 등록할 대상 회원의 id 입니다.")
    @PostMapping("/positive/{memberId}")
    public ApiResponse<MannerInsertResponse> addPositiveMannerRating(
            @PathVariable(name = "memberId") Long targetMemberId,
            @Valid @RequestBody MannerInsertRequest request,
            @AuthMember Member member) {
        return ApiResponse.ok(mannerFacadeService.insertPositiveMannerRating(member, targetMemberId, request));
    }

}
