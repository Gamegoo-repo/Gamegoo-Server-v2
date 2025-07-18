package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRepository;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardDeleteRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.*;
import com.gamegoo.gamegoo_v2.core.common.validator.BanValidator;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardFacadeService {

    private final BoardService boardService;
    private final BoardGameStyleService boardGameStyleService;
    private final FriendService friendService;
    private final BlockService blockService;
    private final ProfanityCheckService profanityCheckService;
    private final MannerService mannerService;
    private final BanValidator banValidator;
    private final MemberRepository memberRepository;

    /**
     * 게시글 생성 (파사드)
     * - DTO -> 엔티티 변환 및 저장
     * - 연관된 GameStyle(BoardGameStyle) 매핑 처리
     * - 결과를 BoardInsertResponse로 변환하여 반환
     */
    @Transactional
    public BoardInsertResponse createBoard(BoardInsertRequest request, Member member) {
        // 게시글 작성 제재 검증
        banValidator.throwIfBannedFromPosting(member);

        profanityCheckService.validateProfanity(request.getContents());
        Board board = boardService.createAndSaveBoard(request, member);
        boardGameStyleService.mapGameStylesToBoard(board, request.getGameStyles());

        return BoardInsertResponse.of(board, member);
    }

    /**
     * 게스트 게시글 생성 (파사드)
     * - 소환사명 + 태그로 기존 회원 확인
     * - 기존 회원이면 예외 발생
     * - 게스트 게시글 생성
     */
    @Transactional
    public BoardInsertResponse createGuestBoard(GuestBoardInsertRequest request, String gameName, String tag) {
        // 기존 회원 확인
        if (memberRepository.existsByGameNameAndTag(gameName, tag)) {
            throw new BoardException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        profanityCheckService.validateProfanity(request.getContents());
        Board board = boardService.createAndSaveGuestBoard(request, gameName, tag, request.getPassword());
        boardGameStyleService.mapGameStylesToBoard(board, request.getGameStyles());

        return BoardInsertResponse.ofGuest(board);
    }

    /**
     * 게시판 글 목록 조회 (파사드)
     */
    public BoardResponse getBoardList(GameMode gameMode, Tier tier, Position mainP, Position subP, Mike mike, int pageIdx) {
        if (mainP == null) {
            mainP = Position.ANY;
        }
        if (subP == null) {
            subP = Position.ANY;
        }

        Page<Board> boardPage = boardService.getBoardsWithPagination(gameMode, tier, mainP, subP, mike, pageIdx);
        return BoardResponse.of(boardPage);
    }

    /**
     * 회원 게시판 글 단건 조회 (파사드)
     * - "회원 전용" 조회 로직
     */
    public BoardByIdResponseForMember getBoardByIdForMember(Long boardId, Member viewer) {

        Board board = boardService.findBoard(boardId);

        boolean isBlocked = blockService.isBlocked(viewer, board.getMember());
        boolean isFriend = friendService.isFriend(viewer, board.getMember());
        Long friendRequestMemberId = friendService.getFriendRequestMemberId(viewer, board.getMember());

        return BoardByIdResponseForMember.of(board, isBlocked, isFriend, friendRequestMemberId, mannerService);
    }

    /**
     * 비회원 게시판 글 단건 조회 (파사드)
     * - "비회원 전용" 조회 로직
     */
    public BoardByIdResponse getBoardById(Long boardId) {

        Board board = boardService.findBoard(boardId);

        return BoardByIdResponse.of(board);
    }

    /**
     * 게시글 수정 (파사드)
     *
     * @param request 게시글 수정 요청 DTO
     * @param member  현재 로그인한 Member
     * @param boardId 수정할 게시글 ID
     * @return 수정된 게시글 정보(Response)
     */
    @Transactional
    public BoardUpdateResponse updateBoard(BoardUpdateRequest request, Member member, Long boardId) {
        // 게시글 작성 제재 검증
        banValidator.throwIfBannedFromPosting(member);

        profanityCheckService.validateProfanity(request.getContents());
        Board board = boardService.updateBoard(request, member.getId(), boardId);
        boardGameStyleService.updateBoardGameStyles(board, request.getGameStyles());

        return BoardUpdateResponse.of(board);

    }

    /**
     * 게시글 삭제 (파사드)
     *
     * @param member  현재 로그인한 Member
     * @param boardId 삭제할 게시글 ID
     */
    @Transactional
    public void deleteBoard(Member member, Long boardId) {
        boardService.deleteBoard(boardId, member.getId());
    }

    /**
     * 비회원 게시글 수정 (파사드)
     */
    @Transactional
    public BoardUpdateResponse updateGuestBoard(GuestBoardUpdateRequest request, Long boardId) {
        profanityCheckService.validateProfanity(request.getContents());
        Board board = boardService.updateGuestBoard(request, boardId);
        boardGameStyleService.updateBoardGameStyles(board, request.getGameStyles());

        return BoardUpdateResponse.of(board);
    }

    /**
     * 비회원 게시글 삭제 (파사드)
     */
    @Transactional
    public void deleteGuestBoard(Long boardId, GuestBoardDeleteRequest request) {
        boardService.deleteGuestBoard(boardId, request.getPassword());
    }

    /**
     * 내가 작성한 게시글 목록 조회 (파사드)
     */
    public MyBoardResponse getMyBoardList(Member member, int pageIdx) {
        Page<Board> boardPage = boardService.getMyBoards(member.getId(), pageIdx);
        return MyBoardResponse.of(boardPage);
    }

    /**
     * 내가 작성한 게시글 목록 조회(커서)
     */
    public MyBoardCursorResponse getMyBoardCursorList(Member member, LocalDateTime cursor) {
        Slice<Board> boardSlice = boardService.getMyBoards(member.getId(), cursor);
        return MyBoardCursorResponse.of(boardSlice);
    }

    /**
     * 게시글 끌올(bump) 기능 (파사드)
     * 사용자가 "끌올" 버튼을 누르면 해당 게시글의 bumpTime을 업데이트합니다.
     * 단, 마지막 끌올 후 1시간이 지나지 않았다면 예외를 발생시킵니다.
     */

    @Transactional
    public BoardBumpResponse bumpBoard(Long boardId, Member member) {
        // 게시글 작성 제재 검증
        banValidator.throwIfBannedFromPosting(member);

        Board board = boardService.bumpBoard(boardId, member.getId());
        return BoardBumpResponse.of(board.getId(), board.getBumpTime());
    }

    /**
     * 전체 게시글 커서 기반 조회 (Secondary Cursor)
     */
    public BoardCursorResponse getAllBoardsWithCursor(
            LocalDateTime cursor,
            Long cursorId,
            GameMode gameMode,
            Tier tier,
            Position position1,
            Position position2) {
        Slice<Board> boardSlice = boardService.getAllBoardsWithCursor(cursor, cursorId, gameMode, tier, position1, position2);
        return BoardCursorResponse.of(boardSlice);
    }

}
