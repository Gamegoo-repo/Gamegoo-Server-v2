package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.*;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidPage;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import com.gamegoo.gamegoo_v2.social.manner.service.MannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardFacadeService {

    private final BoardService boardService;
    private final BoardGameStyleService boardGameStyleService;
    private final MemberService memberService;
    private final FriendService friendService;
    private final BlockService blockService;
    private final ProfanityCheckService profanityCheckService;
    private final MannerService mannerService;

    /**
     * 게시글 생성 (파사드)
     * - DTO -> 엔티티 변환 및 저장
     * - 연관된 GameStyle(BoardGameStyle) 매핑 처리
     * - 결과를 BoardInsertResponse로 변환하여 반환
     */
    @Transactional
    public BoardInsertResponse createBoard(BoardInsertRequest request, Member member) {

        profanityCheckService.validateProfanity(request.getContents());
        Board board = boardService.createAndSaveBoard(request, member);
        boardGameStyleService.mapGameStylesToBoard(board, request.getGameStyles());

        return BoardInsertResponse.of(board, member);
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
     * 내가 작성한 게시글 목록 조회 (파사드)
     */
    public MyBoardResponse getMyBoardList(Member member, int pageIdx) {
        Page<Board> boardPage = boardService.getMyBoards(member.getId(), pageIdx);
        return MyBoardResponse.of(boardPage);
    }

    /**
     * 내가 작성한 게시글 목록 조회(커서)
     */
    public MyBoardCursorResponse getMyBoardCursorList(Member member, Long cursor) {
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
        Board board = boardService.bumpBoard(boardId, member.getId());
        return BoardBumpResponse.of(board.getId(), board.getBumpTime());
    }

}
