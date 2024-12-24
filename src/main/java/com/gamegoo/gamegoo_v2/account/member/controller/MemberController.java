package com.gamegoo.gamegoo_v2.account.member.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
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
    public ApiResponse<String> modifyPosition(
            @Valid @RequestBody ProfileImageRequest request, @AuthMember Member member) {

        return ApiResponse.ok(memberFacadeService.setProfileImage(member, request));
    }

}
