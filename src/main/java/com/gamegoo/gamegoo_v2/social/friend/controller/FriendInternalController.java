package com.gamegoo.gamegoo_v2.social.friend.controller;

import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.config.swagger.ApiErrorCodes;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Friend Internal", description = "친구 관련 internal API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/internal")
public class FriendInternalController {

    private final FriendFacadeService friendFacadeService;

    @Operation(summary = "모든 친구 id 조회 API", description = "해당 회원의 모든 친구 id 목록을 조회하는 API 입니다. " +
            "정렬 기능 없음, socket서버용 API입니다.")
    @GetMapping(value = "/{memberId}/friend/ids")
    @ApiErrorCodes({ErrorCode.MEMBER_NOT_FOUND})
   public ApiResponse<List<Long>> getFriendIds(@PathVariable(name = "memberId") Long memberId) {
        return ApiResponse.ok(friendFacadeService.getFriendIdList(memberId));
    }

}
