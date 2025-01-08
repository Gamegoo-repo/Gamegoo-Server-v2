package com.gamegoo.gamegoo_v2.social.manner.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerInsertRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.request.MannerUpdateRequest;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerInsertResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerRatingResponse;
import com.gamegoo.gamegoo_v2.social.manner.dto.response.MannerUpdateResponse;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @Operation(summary = "비매너 평가 등록 API", description = "비매너 평가를 등록하는 API 입니다.")
    @Parameter(name = "memberId", description = "비매너 평가를 등록할 대상 회원의 id 입니다.")
    @PostMapping("/negative/{memberId}")
    public ApiResponse<MannerInsertResponse> addNegativeMannerRating(
            @PathVariable(name = "memberId") Long targetMemberId,
            @Valid @RequestBody MannerInsertRequest request,
            @AuthMember Member member) {
        return ApiResponse.ok(mannerFacadeService.insertNegativeMannerRating(member, targetMemberId, request));
    }

    @Operation(summary = "매너/비매너 평가 수정 API", description = "매너/비매너 평가를 수정하는 API 입니다.")
    @Parameter(name = "mannerId", description = "수정하고자 하는 매너/비매너 평가 id 입니다.")
    @PutMapping("/{mannerId}")
    public ApiResponse<MannerUpdateResponse> updateMannerRating(
            @PathVariable(name = "mannerId") Long mannerId,
            @Valid @RequestBody MannerUpdateRequest request,
            @AuthMember Member member) {
        return ApiResponse.ok(mannerFacadeService.updateMannerRating(member, mannerId, request));
    }

    @Operation(summary = "특정 회원에 대한 나의 매너 평가 조회 API", description = "특정 회원에 대해 내가 실시한 매너 평가를 조회하는 API 입니다.")
    @Parameter(name = "memberId", description = "대상 회원의 id 입니다.")
    @GetMapping("/positive/{memberId}")
    public ApiResponse<MannerRatingResponse> getPositiveMannerRatingInfo(
            @PathVariable(name = "memberId") Long targetMemberId,
            @AuthMember Member member) {
        return ApiResponse.ok(mannerFacadeService.getMannerRating(member, targetMemberId, true));
    }

    @Operation(summary = "특정 회원에 대한 나의 비매너 평가 조회 API", description = "특정 회원에 대해 내가 실시한 비매너 평가를 조회하는 API 입니다.")
    @Parameter(name = "memberId", description = "대상 회원의 id 입니다.")
    @GetMapping("/negative/{memberId}")
    public ApiResponse<MannerRatingResponse> getNegativeMannerRatingInfo(
            @PathVariable(name = "memberId") Long targetMemberId,
            @AuthMember Member member) {
        return ApiResponse.ok(mannerFacadeService.getMannerRating(member, targetMemberId, false));
    }

    @Operation(summary = "특정 회원의 매너 정보 조회 API", description = "특정 회원의 매너 정보를 조회하는 API 입니다.")
    @Parameter(name = "memberId", description = "대상 회원의 id 입니다.")
    @GetMapping("/{memberId}")
    public ApiResponse<Object> getMyManner(@PathVariable(name = "memberId") Long memberId) {
        return ApiResponse.ok(mannerFacadeService.getMannerInfo(memberId));
    }
}
