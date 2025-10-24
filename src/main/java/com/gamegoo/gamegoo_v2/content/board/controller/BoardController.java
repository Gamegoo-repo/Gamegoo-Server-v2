package com.gamegoo.gamegoo_v2.content.board.controller;

import com.gamegoo.gamegoo_v2.account.auth.annotation.AuthMember;
import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardDeleteRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.*;
import com.gamegoo.gamegoo_v2.content.board.service.BoardFacadeService;
import com.gamegoo.gamegoo_v2.core.common.ApiResponse;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidCursor;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidPage;
import com.gamegoo.gamegoo_v2.core.config.swagger.ApiErrorCodes;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

import java.time.LocalDateTime;

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
    @ApiErrorCodes({
            ErrorCode.MEMBER_BANNED_FROM_POSTING,
            ErrorCode.BOARD_FORBIDDEN_WORD,
            ErrorCode.BOARD_CREATE_COOL_DOWN,
            ErrorCode.BOARD_GAME_STYLE_NOT_FOUND
    })
    public ApiResponse<BoardInsertResponse> boardInsert(
            @AuthMember Member member,
            @Valid @RequestBody BoardInsertRequest request) {
        return ApiResponse.ok(boardFacadeService.createBoard(request, member));
    }

    @PostMapping("/guest")
    @Operation(summary = "비회원 게시판 글 작성 API",
            description = "비회원이 게시판에서 글을 작성하는 API 입니다. 프로필이미지 값: 1~8, gameMode: < 빠른대전: FAST, 솔로랭크: SOLO, 자유랭크: FREE, 칼바람 " +
                    "나락: ARAM >, " +
                    "주 포지션, 부포지션, 희망 포지션: < TOP, JUNGLE, MID, ADC, SUP, ANY >, 마이크 여부: < AVAILABLE, UNAVAILABLE >, 게임" +
                    " 스타일 리스트: 1~3개 선택 가능")
    @ApiErrorCodes({
            ErrorCode.BOARD_FORBIDDEN_WORD,
            ErrorCode.BOARD_GAME_STYLE_NOT_FOUND,
            ErrorCode.RIOT_INVALID_API_KEY,
            ErrorCode.RIOT_NOT_FOUND,
            ErrorCode.RIOT_API_ERROR,
            ErrorCode.RIOT_SERVER_ERROR,
            ErrorCode.RIOT_NETWORK_ERROR,
            ErrorCode.RIOT_UNKNOWN_ERROR,
            ErrorCode.MEMBER_ALREADY_EXISTS
    })
    public ApiResponse<BoardInsertResponse> guestBoardInsert(
            @Valid @RequestBody GuestBoardInsertRequest request) {
        return ApiResponse.ok(boardFacadeService.createGuestBoard(request, request.getGameName(), request.getTag()));
    }

    /**
     * 게시글 목록 조회 API
     */
    @GetMapping("/list")
    @Operation(summary = "게시판 글 목록 조회 API",
            description = "게시판 글 목록을 조회하는 API 입니다. 필터링을 원하면 각 파라미터를 입력하세요.")
    @Parameters({
            @Parameter(name = "gameMode", description = "(선택) 게임 모드를 입력해주세요. < 빠른대전: FAST, 솔로랭크: SOLO, 자유랭크: FREE, " +
                    "칼바람 나락: ARAM >"),
            @Parameter(name = "tier", description = "(선택) 티어를 선택해주세요."),
            @Parameter(name = "mainP", description = "(선택) 주 포지션을 입력해주세요. < 전체: ANY, 탑: TOP, 정글: JUNGLE, 미드: " +
                    "MID, 원딜: ADC, " +
                    "서포터: SUP >"),
            @Parameter(name = "subP", description = "(선택) 부 포지션을 입력해주세요. < 전체: ANY, 탑: TOP, 정글: JUNGLE, 미드: " +
                    "MID, 원딜: ADC, " +
                    "서포터: SUP >"),
            @Parameter(name = "mike", description = "(선택) 마이크 여부를 선택해주세요.")
    })
    @ApiErrorCodes({ErrorCode._BAD_REQUEST})
    public ApiResponse<BoardResponse> boardList(
            @ValidPage @RequestParam(name = "page") Integer page,
            @Parameter(description = "게임 모드", schema = @Schema(ref = "#/components/schemas/GameMode"))
            @RequestParam(required = false) GameMode gameMode,
            @Parameter(description = "티어", schema = @Schema(ref = "#/components/schemas/Tier"))
            @RequestParam(required = false) Tier tier,
            @Parameter(description = "메인 포지션", schema = @Schema(ref = "#/components/schemas/Position"))
            @RequestParam(required = false) Position mainP,
            @Parameter(description = "서브 포지션", schema = @Schema(ref = "#/components/schemas/Position"))
            @RequestParam(required = false) Position subP,
            @Parameter(description = "마이크 사용 여부", schema = @Schema(ref = "#/components/schemas/Mike"))
            @RequestParam(required = false) Mike mike) {

        return ApiResponse.ok(boardFacadeService.getBoardList(gameMode, tier, mainP, subP, mike, page));


    }

    @GetMapping("/member/list/{boardId}")
    @Operation(summary = "회원용 게시판 글 조회 API", description = "게시판에서 글을 조회하는 API 입니다.")
    @Parameter(name = "boardId", description = "조회할 게시판 글 id 입니다.")
    @ApiErrorCodes({ErrorCode.BOARD_NOT_FOUND})
    public ApiResponse<BoardByIdResponseForMember> getBoardByIdForMember(@PathVariable Long boardId,
                                                                         @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.getBoardByIdForMember(boardId, member));
    }

    @GetMapping("/list/{boardId}")
    @Operation(summary = "비회원용 게시판 글 조회 API", description = "게시판에서 글을 조회하는 API 입니다.")
    @Parameter(name = "boardId", description = "조회할 게시판 글 id 입니다.")
    @ApiErrorCodes({ErrorCode.BOARD_NOT_FOUND})
    public ApiResponse<BoardByIdResponse> getBoardById(@PathVariable Long boardId) {
        return ApiResponse.ok(boardFacadeService.getBoardById(boardId));
    }

    @PutMapping("/{boardId}")
    @Operation(summary = "게시판 글 수정 API", description = "게시판에서 글을 수정하는 API 입니다.")
    @Parameter(name = "boardId", description = "수정할 게시판 글 id 입니다.")
    @ApiErrorCodes({
            ErrorCode.MEMBER_BANNED_FROM_POSTING,
            ErrorCode.BOARD_FORBIDDEN_WORD,
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.UPDATE_BOARD_ACCESS_DENIED,
            ErrorCode.BOARD_GAME_STYLE_NOT_FOUND
    })
    public ApiResponse<BoardUpdateResponse> boardUpdate(@PathVariable Long boardId,
                                                        @Valid @RequestBody BoardUpdateRequest request,
                                                        @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.updateBoard(request, member, boardId));
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "게시판 글 삭제 API", description = "게시판에서 글을 삭제하는 API 입니다.")
    @Parameter(name = "boardId", description = "삭제할 게시판 글 id 입니다.")
    @ApiErrorCodes({
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.DELETE_BOARD_ACCESS_DENIED
    })
    public ApiResponse<String> delete(@PathVariable Long boardId, @AuthMember Member member) {
        boardFacadeService.deleteBoard(member, boardId);
        return ApiResponse.ok("게시글을 삭제하였습니다.");
    }

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 게시판 글 목록 조회 API", description = "내가 작성한 게시판 글을 조회하는 API 입니다. 페이지 당 10개의 게시물이 표시됩니다.")
    @ApiErrorCodes({
            ErrorCode.UNAUTHORIZED_EXCEPTION,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INACTIVE_MEMBER,
            ErrorCode._BAD_REQUEST
    })
    public ApiResponse<MyBoardResponse> getMyBoardList(@ValidPage @RequestParam(name = "page") Integer page,
                                                       @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.getMyBoardList(member, page));
    }

    @GetMapping("/my/cursor")
    @Operation(summary = "내가 작성한 게시판 글 목록 조회 API/모바일", description = "모바일에서 내가 작성한 게시판 글을 조회하는 API 입니다.")
    @Parameter(name = "cursor", description = "페이징을 위한 커서, ISO 8601 형식의 LocalDateTime을 보내주세요. " + "보내지 않으면 가장 최근 게시물 10개를 조회합니다.")
    @ApiErrorCodes({
            ErrorCode.UNAUTHORIZED_EXCEPTION,
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.INACTIVE_MEMBER,
            ErrorCode._BAD_REQUEST
    })
    public ApiResponse<MyBoardCursorResponse> getMyBoardCursorList(
            @ValidCursor @RequestParam(name = "cursor", required = false) LocalDateTime cursor,
            @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.getMyBoardCursorList(member, cursor));
    }


    /**
     * 게시글 끌올(bump) API
     * 사용자가 "끌올" 버튼을 누르면 해당 게시글의 bumpTime이 업데이트되어 상단으로 노출됩니다.
     */
    @PostMapping("/{boardId}/bump")
    @Operation(summary = "게시글 끌올 API", description = "게시글을 끌올하여 상단 노출시키는 API 입니다. 마지막 끌올 후 5분 제한이 적용됩니다.")
    @Parameter(name = "boardId", description = "끌올할 게시판 글 id 입니다.")
    @ApiErrorCodes({
            ErrorCode.MEMBER_BANNED_FROM_POSTING,
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.BUMP_ACCESS_DENIED,
            ErrorCode.BUMP_TIME_LIMIT
    })
    public ApiResponse<BoardBumpResponse> bumpBoard(@PathVariable Long boardId,
                                                    @AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.bumpBoard(boardId, member));
    }

    /**
     * 최신글 자동 끌올 API
     * 사용자가 작성한 가장 최근 게시글을 자동으로 끌올합니다.
     * boardId를 지정할 필요 없이 한 번의 호출로 끌올이 가능합니다.
     */
    @PostMapping("/my/latest/bump")
    @Operation(
        summary = "최신글 자동 끌올 API",
        description = "사용자가 작성한 가장 최근 게시글을 자동으로 끌올하는 API 입니다. 마지막 끌올 후 5분 제한이 적용됩니다."
    )
    @ApiErrorCodes({
            ErrorCode.MEMBER_BANNED_FROM_POSTING,
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.BUMP_TIME_LIMIT
    })
    public ApiResponse<BoardBumpResponse> bumpLatestBoard(@AuthMember Member member) {
        return ApiResponse.ok(boardFacadeService.bumpLatestBoard(member));
    }

    @GetMapping("/cursor")
    @Operation(
        summary = "커서 기반 게시판 글 목록 조회 API",
        description = "커서 기반(무한 스크롤)으로 게시판 글 목록을 조회하는 API 입니다. 최신 글부터 순차적으로 내려가며, 커서와 cursorId를 이용해 다음 페이지를 조회할 수 있습니다. 필터링을 원하면 각 파라미터를 입력하세요."
    )
    @Parameters({
        @Parameter(name = "cursor", description = "(선택) 페이징을 위한 커서, ISO 8601 형식의 LocalDateTime을 보내주세요. 없으면 최신글부터 조회합니다."),
        @Parameter(name = "cursorId", description = "(선택) 커서와 동일한 activityTime을 가진 게시글 중 마지막 게시글의 id. 커서 페이징에 사용됩니다."),
        @Parameter(name = "gameMode", description = "(선택) 게임 모드를 입력해주세요. < 빠른대전: FAST, 솔로랭크: SOLO, 자유랭크: FREE, 칼바람 나락: ARAM >"),
        @Parameter(name = "tier", description = "(선택) 티어를 선택해주세요."),
        @Parameter(name = "position1", description = "(선택) 주 포지션을 입력해주세요. < 전체: ANY, 탑: TOP, 정글: JUNGLE, 미드: MID, 원딜: ADC, 서포터: SUP >"),
        @Parameter(name = "position2", description = "(선택) 부 포지션을 입력해주세요. < 전체: ANY, 탑: TOP, 정글: JUNGLE, 미드: MID, 원딜: ADC, 서포터: SUP >")
    })
    @ApiErrorCodes({ErrorCode._BAD_REQUEST})
    public ResponseEntity<ApiResponse<BoardCursorResponse>> getBoardsWithCursor(
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(required = false) Long cursorId,
            @Parameter(description = "게임 모드", schema = @Schema(ref = "#/components/schemas/GameMode"))
            @RequestParam(required = false) GameMode gameMode,
            @Parameter(description = "티어", schema = @Schema(ref = "#/components/schemas/Tier"))
            @RequestParam(required = false) Tier tier,
            @Parameter(description = "주 포지션", schema = @Schema(ref = "#/components/schemas/Position"))
            @RequestParam(required = false) Position position1,
            @Parameter(description = "부 포지션", schema = @Schema(ref = "#/components/schemas/Position"))
            @RequestParam(required = false) Position position2) {
        BoardCursorResponse response = boardFacadeService.getAllBoardsWithCursor(cursor, cursorId, gameMode, tier, position1, position2);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/guest/{boardId}")
    @Operation(summary = "비회원 게시판 글 수정 API", description = "비회원이 게시판에서 글을 수정하는 API 입니다.")
    @Parameter(name = "boardId", description = "수정할 게시판 글 id 입니다.")
    @ApiErrorCodes({
            ErrorCode.BOARD_FORBIDDEN_WORD,
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.GUEST_BOARD_ACCESS_DENIED,
            ErrorCode.INVALID_GUEST_PASSWORD,
            ErrorCode.BOARD_GAME_STYLE_NOT_FOUND
    })
    public ApiResponse<BoardUpdateResponse> guestBoardUpdate(@PathVariable Long boardId,
                                                            @Valid @RequestBody GuestBoardUpdateRequest request) {
        return ApiResponse.ok(boardFacadeService.updateGuestBoard(request, boardId));
    }

    @DeleteMapping("/guest/{boardId}")
    @Operation(summary = "비회원 게시판 글 삭제 API", description = "비회원이 게시판에서 글을 삭제하는 API 입니다.")
    @Parameter(name = "boardId", description = "삭제할 게시판 글 id 입니다.")
    @ApiErrorCodes({
            ErrorCode.BOARD_NOT_FOUND,
            ErrorCode.GUEST_BOARD_ACCESS_DENIED,
            ErrorCode.INVALID_GUEST_PASSWORD
    })
    public ApiResponse<String> deleteGuestBoard(@PathVariable Long boardId,
                                               @Valid @RequestBody GuestBoardDeleteRequest request) {
        boardFacadeService.deleteGuestBoard(boardId, request);
        return ApiResponse.ok("게시글을 삭제하였습니다.");
    }

}
