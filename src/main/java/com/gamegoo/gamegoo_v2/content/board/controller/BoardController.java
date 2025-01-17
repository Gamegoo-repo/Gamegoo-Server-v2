package com.gamegoo.gamegoo_v2.content.board.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardByIdResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardByIdResponseForMember;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardInsertResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardUpdateResponse;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v2/posts")
@Tag(name = "Board", description = "게시판 관련 API")
public class BoardController {

    private final BoardFacadeService boardFacadeService;

    /**
     * 게시글 작성 API
     */
    @PostMapping
    @Operation(summary = "게시판 글 작성 API",
            description = "게시판에서 글을 작성하는 API 입니다. 게임 모드 1~4, 포지션 0~5를 입력하세요. 게임스타일은 최대 3개까지 입력가능합니다.")
    public ApiResponse<BoardInsertResponse> boardInsert(
            @AuthMember Member member,
            @Valid @RequestBody BoardInsertRequest request) {
        return ApiResponse.ok(boardFacadeService.createBoard(request, member));
    }

    /**
     * 게시글 목록 조회 API
     */
    @GetMapping("/list")
    @Operation(summary = "게시판 글 목록 조회 API",
            description = "게시판 글 목록을 조회하는 API 입니다. 필터링을 원하면 각 파라미터를 입력하세요.")
    @Parameters({
            @Parameter(name = "pageIdx", description = "조회할 페이지 번호를 입력해주세요. 페이지 당 20개의 게시물을 볼 수 있습니다."),
            @Parameter(name = "mode", description = "(선택) 게임 모드를 입력해주세요. < 빠른대전: 1, 솔로랭크: 2, 자유랭크: 3, 칼바람 나락: 4 >"),
            @Parameter(name = "tier", description = "(선택) 티어를 선택해주세요."),
            @Parameter(name = "mainPosition", description = "(선택) 포지션을 입력해주세요. < 전체: 0, 탑: 1, 정글: 2, 미드: 3, 바텀: 4, " +
                    "서포터: 5 >"),
            @Parameter(name = "mike", description = "(선택) 마이크 여부를 선택해주세요.")
    })
    public ApiResponse<BoardResponse> boardList(
            @ValidPage @RequestParam(name = "page") Integer page,
            @RequestParam(required = false) Integer mode,
            @RequestParam(required = false) Tier tier,
            @RequestParam(required = false) Integer mainPosition,
            @RequestParam(required = false) Boolean mike) {

        return ApiResponse.ok(boardFacadeService.getBoardList(mode, tier, mainPosition, mike, page));


    }

    @GetMapping("/member/list/{boardId}")
    @Operation(summary = "회원용 게시판 글 조회 API", description = "게시판에서 글을 조회하는 API 입니다.")
    @Parameter(name = "boardId", description = "조회할 게시판 글 id 입니다.")
    public ApiResponse<BoardByIdResponseForMember> getBoardByIdForMember(@PathVariable Long boardId,
                                                                         @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.getBoardByIdForMember(boardId, member));
    }

    @GetMapping("/list/{boardId}")
    @Operation(summary = "비회원용 게시판 글 조회 API", description = "게시판에서 글을 조회하는 API 입니다.")
    @Parameter(name = "boardId", description = "조회할 게시판 글 id 입니다.")
    public ApiResponse<BoardByIdResponse> getBoardById(@PathVariable Long boardId) {
        return ApiResponse.ok(boardFacadeService.getBoardById(boardId));
    }

    @PutMapping("/{boardId}")
    @Operation(summary = "게시판 글 수정 API", description = "게시판에서 글을 수정하는 API 입니다.")
    @Parameter(name = "boardId", description = "수정할 게시판 글 id 입니다.")
    public ApiResponse<BoardUpdateResponse> boardUpdate(@PathVariable Long boardId,
                                                        @Valid @RequestBody BoardUpdateRequest request,
                                                        @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.updateBoard(request, member, boardId));
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "게시판 글 삭제 API", description = "게시판에서 글을 삭제하는 API 입니다.")
    @Parameter(name = "boardId", description = "삭제할 게시판 글 id 입니다.")
    public ApiResponse<String> delete(@PathVariable Long boardId, @AuthMember Member member) {
        boardFacadeService.deleteBoard(member, boardId);
        return ApiResponse.ok("게시글을 삭제하였습니다.");
    }


}
