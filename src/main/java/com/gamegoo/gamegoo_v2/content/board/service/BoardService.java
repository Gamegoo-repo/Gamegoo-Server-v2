package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    public static final int PAGE_SIZE = 20;
    public static final int MY_PAGE_SIZE = 10;

    /**
     * 게시글 엔티티 생성 및 저장
     */
    @Transactional
    public Board createAndSaveBoard(BoardInsertRequest request, Member member) {
        int boardProfileImage = (request.getBoardProfileImage() != null)
                ? request.getBoardProfileImage()
                : member.getProfileImage();

        Board board = Board.create(
                member,
                request.getGameMode(),
                request.getMainPosition(),
                request.getSubPosition(),
                request.getWantPosition(),
                request.getMike(),
                request.getContents(),
                boardProfileImage
        );
        return boardRepository.save(board);
    }

    /**
     * 게시글 목록 조회
     */
    public Page<Board> findBoards(Integer mode, Tier tier, Position mainPosition, Mike mike, Pageable pageable) {
        return boardRepository.findByFilters(mode, tier, mainPosition, mike, pageable);
    }

    /**
     * 게시글 목록 조회 (페이징 처리)
     */

    public Page<Board> getBoardsWithPagination(Integer mode, Tier tier, Position mainPosition, Mike mike,
                                               int pageIdx) {
        Pageable pageable = PageRequest.of(pageIdx - 1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return findBoards(mode, tier, mainPosition, mike, pageable);
    }

    /**
     * 게시글 엔티티 조회
     */
    public Board findBoard(Long boardId) {
        return boardRepository.findByIdAndDeleted(boardId, false).orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));
    }

    /**
     * 게시글 수정 로직
     */
    @Transactional
    public Board updateBoard(BoardUpdateRequest request, Long memberId, Long boardId) {

        Board board =
                boardRepository.findByIdAndDeleted(boardId, false).orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardException(ErrorCode.UPDATE_BOARD_ACCESS_DENIED);
        }

        board.updateBoard(
                request.getGameMode(),
                request.getMainPosition(),
                request.getSubPosition(),
                request.getWantPosition(),
                request.getMike(),
                request.getContents(),
                request.getBoardProfileImage()
        );

        return board;
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deleteBoard(Long boardId, Long memberId) {
        Board board =
                boardRepository.findByIdAndDeleted(boardId, false).orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardException(ErrorCode.DELETE_BOARD_ACCESS_DENIED);
        }

        board.setDeleted(true);
        boardRepository.save(board);
    }

    /**
     * 내가 작성한 게시글(Page) 조회
     */
    public Page<Board> getMyBoards(Long memberId, int pageIdx) {
        if (pageIdx <= 0) {
            throw new IllegalArgumentException("pageIdx는 1 이상의 값이어야 합니다.");
        }
        // PageRequest.of의 첫 번째 인자(pageIdx - 1)는 0-based index
        Pageable pageable = PageRequest.of(pageIdx - 1, MY_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return boardRepository.findByMemberIdAndDeletedFalse(memberId, pageable);
    }

    /**
     * Board 저장
     */
    @Transactional
    public Board saveBoard(Board board) {
        return boardRepository.save(board);
    }

}
