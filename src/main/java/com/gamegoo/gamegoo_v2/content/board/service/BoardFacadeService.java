package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.account.member.service.MemberService;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardByIdResponseForMember;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardInsertResponse;
import com.gamegoo.gamegoo_v2.content.board.dto.response.BoardResponse;
import com.gamegoo.gamegoo_v2.core.common.annotation.ValidPage;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.social.block.service.BlockService;
import com.gamegoo.gamegoo_v2.social.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardFacadeService {

    private final BoardService boardService;
    private final BoardGameStyleService boardGameStyleService;
    private final MemberService memberService;
    private final FriendService friendService;
    private final BlockService blockService;

    /**
     * 게시글 생성 (파사드)
     * - DTO -> 엔티티 변환 및 저장
     * - 연관된 GameStyle(BoardGameStyle) 매핑 처리
     * - 결과를 BoardInsertResponse로 변환하여 반환
     */
    @Transactional
    public BoardInsertResponse createBoard(BoardInsertRequest request, Member member) {

        Board board = boardService.createAndSaveBoard(request, member);
        boardGameStyleService.mapGameStylesToBoard(board, request.getGameStyles());

        return BoardInsertResponse.of(board, member);
    }

    /**
     * 게시판 글 목록 조회 (파사드)
     */

    public BoardResponse getBoardList(Integer mode, Tier tier, Integer mainPosition, Boolean mike,
                                      @ValidPage int pageIdx) {

        // <포지션 정보> 전체: 0, 탑: 1, 정글: 2, 미드: 3, 바텀: 4, 서포터: 5
        if (mainPosition != null && mainPosition == 0) {
            mainPosition = null;
        }

        Page<Board> boardPage = boardService.getBoardsWithPagination(mode, tier, mainPosition, mike, pageIdx);

        return BoardResponse.of(boardPage);
    }

    /**
     * 회원 게시판 글 단건 조회 (파사드)
     * - “회원 전용” 조회 로직
     */
    public BoardByIdResponseForMember getBoardByIdForMember(Long boardId, Long memberId) {

        Board board = boardService.findBoard(boardId);

        if (board.isDeleted()) {
            throw new BoardException(ErrorCode.BOARD_NOT_FOUND);
        }

        Member viewer = memberService.findMemberById(memberId);
        
        boolean isBlocked = blockService.isBlocked(viewer, board.getMember());
        boolean isFriend = friendService.isFriend(viewer, board.getMember());
        Long friendRequestMemberId = friendService.getFriendRequestMemberId(viewer, board.getMember());

        return BoardByIdResponseForMember.of(board, viewer, isBlocked, isFriend, friendRequestMemberId);
    }

}
