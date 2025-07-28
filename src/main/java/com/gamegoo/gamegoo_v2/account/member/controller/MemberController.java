package com.gamegoo.gamegoo_v2.account.member.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.auth.domain.Role;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.dto.request.GameStyleRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.IsMikeRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.PositionRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.request.ProfileImageRequest;
import com.gamegoo.gamegoo_v2.account.member.dto.response.MyProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.dto.response.OtherProfileResponse;
import com.gamegoo.gamegoo_v2.account.member.service.MemberFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/profile")
@Validated
public class MemberController {

    private final MemberFacadeService memberFacadeService;

    @Operation(summary = "내 프로필 조회 API 입니다. (jwt 토큰 O)", description = "API for looking up member with jwt")
    @GetMapping
    public ApiResponse<MyProfileResponse> getMemberJWT(@AuthMember Member member) {
        return ApiResponse.ok(memberFacadeService.getMyProfile(member));
    }

    @Operation(summary = "다른 회원 프로필 조회 API 입니다. (jwt 토큰 O)", description = "API for looking up other member with jwt")
    @GetMapping("/other")
    public ApiResponse<OtherProfileResponse> getMember(@AuthMember Member member,
                                                       @RequestParam("id") Long targetMemberId) {
        return ApiResponse.ok(memberFacadeService.getOtherProfile(member, targetMemberId));
    }

    @Operation(summary = "프로필 이미지 수정 API 입니다.", description = "API for Profile Image Modification")
    @PutMapping("/profileImage")
    public ApiResponse<String> modifyProfileImage(
            @Valid @RequestBody ProfileImageRequest request, @AuthMember Member member) {
        return ApiResponse.ok(memberFacadeService.setProfileImage(member, request));
    }

    @Operation(summary = "마이크 여부 수정 API 입니다.", description = "API for isMike Modification")
    @PutMapping("/mike")
    public ApiResponse<String> modifyIsMike(
            @Valid @RequestBody IsMikeRequest request, @AuthMember Member member) {
        return ApiResponse.ok(memberFacadeService.setMike(member, request));
    }

    @Operation(summary = "주/부/원하는 포지션 수정 API 입니다.", description = "API for Main/Sub/Want Position Modification")
    @PutMapping("/position")
    public ApiResponse<String> modifyPosition(
            @Valid @RequestBody PositionRequest request, @AuthMember Member member) {
        return ApiResponse.ok(memberFacadeService.setPosition(member, request));
    }

    @Operation(summary = "gamestyle 추가 및 수정 API 입니다.", description = "API for Gamestyle addition and modification ")
    @PutMapping("/gamestyle")
    public ApiResponse<String> addGameStyle(@Valid @RequestBody GameStyleRequest request,
                                            @AuthMember Member member) {
        return ApiResponse.ok(memberFacadeService.setGameStyle(member, request));
    }

    @Operation(summary = "챔피언 통계 새로고침 API 입니다.", description = "API for refreshing champion statistics")
    @PutMapping("/champion-stats/refresh")
    public ApiResponse<MyProfileResponse> refreshChampionStats(
            @AuthMember Member member,
            @RequestParam(value = "memberId", required = false) Long targetMemberId) {
        return ApiResponse.ok(memberFacadeService.refreshChampionStats(member, targetMemberId));
    }

    @Operation(summary = "어드민 권한 부여 API (개발용)", description = "개발용 어드민 권한 부여 API")
    @PatchMapping("/admin/grant/{memberId}")
    public ApiResponse<String> grantAdminRole(@PathVariable Long memberId) {

        return ApiResponse.ok(memberFacadeService.updateMemberRole(memberId, Role.ADMIN));
    }

    @Operation(summary = "일반 사용자 권한으로 변경 API (개발용)", description = "어드민 권한을 일반 사용자로 변경하는 API")
    @PatchMapping("/admin/revoke/{memberId}")
    public ApiResponse<String> revokeAdminRole(@PathVariable Long memberId) {
        return ApiResponse.ok(memberFacadeService.updateMemberRole(memberId, Role.MEMBER));
    }

}
