package com.gamegoo.gamegoo_v2.block.controller;

import com.gamegoo.gamegoo_v2.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.block.dto.BlockListResponse;
import com.gamegoo.gamegoo_v2.block.service.BlockFacadeService;
import com.gamegoo.gamegoo_v2.common.ApiResponse;
import com.gamegoo.gamegoo_v2.common.annotation.ValidPage;
import com.gamegoo.gamegoo_v2.member.domain.Member;
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
    public ApiResponse<String> blockMember(@PathVariable(name = "memberId") Long targetMemberId,
            @AuthMember Member member) {
        blockFacadeService.blockMember(member, targetMemberId);
        return ApiResponse.ok("회원 차단 성공");
    }

    @Operation(summary = "차단 목록 조회 API", description = "내가 차단한 회원의 목록을 조회하는 API 입니다.")
    @Parameter(name = "page", description = "페이지 번호, 1 이상의 숫자를 입력해 주세요.")
    @GetMapping
    public ApiResponse<BlockListResponse> getBlockList(@ValidPage @RequestParam(name = "page") Integer page,
            @AuthMember Member member) {
        return ApiResponse.ok(blockFacadeService.getBlockList(member, page));
    }

    @Operation(summary = "회원 차단 해제 API", description = "해당 회원에 대한 차단을 해제하는 API 입니다.")
    @Parameter(name = "memberId", description = "차단을 해제할 대상 회원의 id 입니다.")
    @DeleteMapping("/{memberId}")
    public ApiResponse<String> unblockMember(@PathVariable(name = "memberId") Long targetMemberId,
            @AuthMember Member member) {
        blockFacadeService.unBlockMember(member, targetMemberId);
        return ApiResponse.ok("회원 차단 해제 성공");
    }

}
