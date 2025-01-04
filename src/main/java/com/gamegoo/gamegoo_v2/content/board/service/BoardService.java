package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
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
    public Page<Board> findBoards(Integer mode, Tier tier, Integer mainPosition, Boolean mike, Pageable pageable) {
        return boardRepository.findByFilters(mode, tier, mainPosition, mike, pageable);
    }

    /**
     * 게시글 목록 조회 (페이징 처리)
     */

    public Page<Board> getBoardsWithPagination(Integer mode, Tier tier, Integer mainPosition, Boolean mike,
                                               int pageIdx) {
        Pageable pageable = PageRequest.of(pageIdx - 1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return findBoards(mode, tier, mainPosition, mike, pageable);
    }

    /**
     * 게시글 엔티티 조회
     */
    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));
    }

}
