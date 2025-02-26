package com.gamegoo.gamegoo_v2.content.board.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardBumpResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardByIdResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardByIdResponseForMember;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardInsertResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardUpdateResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.MyBoardResponse;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidPage;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
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
            description = "게시판에서 글을 작성하는 API 입니다. 프로필이미지 값: 1~8, gameMode: < 빠른대전: FAST, 솔로랭크: SOLO, 자유랭크: FREE, 칼바람 " +
                    "나락: ARAM >, " +
                    "주 포지션, 부포지션, 희망 포지션: < TOP, JUNGLE, MID, ADC, SUP, ANY >, 마이크 여부: < AVAILABLE, UNAVAILABLE >, 게임" +
                    " 스타일 리스트: 1~3개 선택 가능")
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
            @Parameter(name = "gameMode", description = "(선택) 게임 모드를 입력해주세요. < 빠른대전: FAST, 솔로랭크: SOLO, 자유랭크: FREE, " +
                    "칼바람 나락: ARAM >"),
            @Parameter(name = "tier", description = "(선택) 티어를 선택해주세요."),
            @Parameter(name = "mainP", description = "(선택) 포지션을 입력해주세요. < 전체: ANY, 탑: TOP, 정글: JUNGLE, 미드: " +
                    "MID, 원딜: ADC, " +
                    "서포터: SUP >"),
            @Parameter(name = "mike", description = "(선택) 마이크 여부를 선택해주세요.")
    })
    public ApiResponse<BoardResponse> boardList(
            @ValidPage @RequestParam(name = "page") Integer page,
            @RequestParam(required = false) GameMode gameMode,
            @RequestParam(required = false) Tier tier,
            @RequestParam(required = false) Position mainP,
            @RequestParam(required = false) Mike mike) {

        return ApiResponse.ok(boardFacadeService.getBoardList(gameMode, tier, mainP, mike, page));


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

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 게시판 글 목록 조회 API", description = "내가 작성한 게시판 글을 조회하는 API 입니다. 페이지 당 10개의 게시물이 표시됩니다.")
    public ApiResponse<MyBoardResponse> getMyBoardList(@ValidPage @RequestParam(name = "page") Integer page,
                                                       @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.getMyBoardList(member, page));
    }

    /**
     * 게시글 끌올(bump) API
     * 사용자가 "끌올" 버튼을 누르면 해당 게시글의 bumpTime이 업데이트되어 상단으로 노출됩니다.
     */
    @PostMapping("/{boardId}/bump")
    @Operation(summary = "게시글 끌올 API", description = "게시글을 끌올하여 상단 노출시키는 API 입니다. 마지막 끌올 후 1시간 제한이 적용됩니다.")
    @Parameter(name = "boardId", description = "끌올할 게시판 글 id 입니다.")
    public ApiResponse<BoardBumpResponse> bumpBoard(@PathVariable Long boardId,
                                                    @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.bumpBoard(boardId, member));
    }

}
