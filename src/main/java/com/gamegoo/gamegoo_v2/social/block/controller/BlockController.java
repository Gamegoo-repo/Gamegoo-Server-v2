package com.gamegoo.gamegoo_v2.social.block.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidPage;
import com.gamegoo.gamegoo_v2.core.config.swagger.ApiErrorCodes;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.block.dto.BlockListResponse;
import com.gamegoo.gamegoo_v2.social.block.dto.BlockResponse;
import com.gamegoo.gamegoo_v2.social.block.service.BlockFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Block", description = "회원 차단 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/block")
@Validated
public class BlockController {

    private final BlockFacadeService blockFacadeService;

    @Operation(summary = "회원 차단 API", description = "대상 회원을 차단하는 API 입니다.")
    @Parameter(name = "memberId", description = "차단할 대상 회원의 id 입니다.")
    @PostMapping("/{memberId}")
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.TARGET_MEMBER_DEACTIVATED,
            ErrorCode.BLOCK_MEMBER_BAD_REQUEST,
            ErrorCode.ALREADY_BLOCKED
    })
    public ApiResponse<BlockResponse> blockMember(@PathVariable(name = "memberId") Long targetMemberId,
                                                  @AuthMember Member member) {
        return ApiResponse.ok(blockFacadeService.blockMember(member, targetMemberId));
    }

    @Operation(summary = "차단 목록 조회 API", description = "내가 차단한 회원의 목록을 조회하는 API 입니다.")
    @Parameter(name = "page", description = "페이지 번호, 1 이상의 숫자를 입력해 주세요.")
    @GetMapping
    @ApiErrorCodes({
            ErrorCode.UNAUTHORIZED_EXCEPTION,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INACTIVE_MEMBER,
            ErrorCode._BAD_REQUEST
    })
    public ApiResponse<BlockListResponse> getBlockList(@ValidPage @RequestParam(name = "page") Integer page,
                                                       @AuthMember Member member) {
        return ApiResponse.ok(blockFacadeService.getBlockList(member, page));
    }

    @Operation(summary = "회원 차단 해제 API", description = "해당 회원에 대한 차단을 해제하는 API 입니다.")
    @Parameter(name = "memberId", description = "차단을 해제할 대상 회원의 id 입니다.")
    @DeleteMapping("/{memberId}")
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.TARGET_MEMBER_DEACTIVATED,
            ErrorCode.TARGET_MEMBER_NOT_BLOCKED
    })
    public ApiResponse<BlockResponse> unblockMember(@PathVariable(name = "memberId") Long targetMemberId,
                                                    @AuthMember Member member) {
        return ApiResponse.ok(blockFacadeService.unBlockMember(member, targetMemberId));
    }

    @Operation(summary = "차단 목록에서 탈퇴한 회원 삭제 API", description = "차단 목록에서 특정 회원이 탈퇴한 회원인 경우, 삭제하는 API 입니다. (차단 해제 아님)")
    @Parameter(name = "memberId", description = "목록에서 삭제할 대상 회원의 id 입니다.")
    @DeleteMapping("/delete/{memberId}")
    @ApiErrorCodes({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.TARGET_MEMBER_NOT_BLOCKED,
            ErrorCode.DELETE_BLOCKED_MEMBER_FAILED
    })
    public ApiResponse<BlockResponse> deleteBlockMember(@PathVariable(name = "memberId") Long targetMemberId,
                                                        @AuthMember Member member) {
        return ApiResponse.ok(blockFacadeService.deleteBlock(member, targetMemberId));
    }

}
