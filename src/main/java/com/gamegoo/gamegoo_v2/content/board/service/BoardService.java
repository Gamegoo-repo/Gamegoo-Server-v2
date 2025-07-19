package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardInsertRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.BoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.dto.request.GuestBoardUpdateRequest;
import com.gamegoo.gamegoo_v2.content.board.repository.BoardRepository;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    public static final int PAGE_SIZE = 10;
    public static final int MY_PAGE_SIZE = 10;
    private static final Duration BUMP_INTERVAL = Duration.ofMinutes(5);

    /**
     * 게시글 엔티티 생성 및 저장
     */
    @Transactional
    public Board createAndSaveBoard(BoardInsertRequest request, Member member) {
        Optional<Board> latestBoardOpt = boardRepository.findTopByMemberIdOrderByCreatedAtDesc(member.getId());
        if (latestBoardOpt.isPresent()) {
            Board latestBoard = latestBoardOpt.get();
            Duration diff = Duration.between(latestBoard.getCreatedAt(), LocalDateTime.now());
            if (diff.compareTo(Duration.ofMinutes(5)) < 0) {
                throw new BoardException(ErrorCode.BOARD_CREATE_COOL_DOWN);
            }
        }
        int boardProfileImage = (request.getBoardProfileImage() != null)
                ? request.getBoardProfileImage()
                : member.getProfileImage();

        Board board = Board.create(
                member,
                request.getGameMode(),
                request.getMainP(),
                request.getSubP(),
                request.getWantP(),
                request.getMike(),
                request.getContents(),
                boardProfileImage
        );
        return boardRepository.save(board);
    }

    /**
     * 게스트 게시글 엔티티 생성 및 저장
     */
    @Transactional
    public Board createAndSaveGuestBoard(BoardInsertRequest request, Member tmpMember, String password) {
        int boardProfileImage = (request.getBoardProfileImage() != null)
                ? request.getBoardProfileImage()
                : tmpMember.getProfileImage();

        Board board = Board.createForGuest(
                tmpMember,
                request.getGameMode(),
                request.getMainP(),
                request.getSubP(),
                request.getWantP(),
                request.getMike(),
                request.getContents(),
                boardProfileImage,
                password
        );
        return boardRepository.save(board);
    }

    /**
     * 게시글 목록 조회
     */
    public Page<Board> findBoards(GameMode gameMode, Tier tier, Position mainP, Position subP, Mike mike,
                                  Pageable pageable) {
        List<Position> mainPList = new ArrayList<>();
        List<Position> subPList = new ArrayList<>();

        // 메인 포지션 처리
        if (mainP == Position.ANY) {
            mainPList = Arrays.asList(Position.values());
        } else {
            mainPList.add(mainP);
        }

        // 부 포지션 처리
        if (subP == Position.ANY) {
            subPList = Arrays.asList(Position.values());
        } else {
            subPList.add(subP);
        }

        return boardRepository.findByGameModeAndTierAndMainPInAndSubPInAndMikeAndDeletedFalse(
                gameMode, tier, mainPList, subPList, mike, pageable);
    }

    /**
     * 게시글 목록 조회 (페이징 처리)
     */
    public Page<Board> getBoardsWithPagination(GameMode gameMode, Tier tier, Position mainP, Position subP, Mike mike, int pageIdx) {
        List<Position> mainPList = new ArrayList<>();
        List<Position> subPList = new ArrayList<>();

        // 메인 포지션 처리
        if (mainP == Position.ANY) {
            mainPList = Arrays.asList(Position.values());
        } else {
            mainPList.add(mainP);
        }

        // 부 포지션 처리
        if (subP == Position.ANY) {
            subPList = Arrays.asList(Position.values());
        } else {
            subPList.add(subP);
        }

        Pageable pageable = PageRequest.of(pageIdx - 1, 10, Sort.by("activityTime").descending());
        return boardRepository.findByGameModeAndTierAndMainPInAndSubPInAndMikeAndDeletedFalse(
                gameMode, tier, mainPList, subPList, mike, pageable);
    }

    /**
     * 게시글 엔티티 조회
     */
    public Board findBoard(Long boardId) {
        return boardRepository.findByIdAndDeleted(boardId, false).orElseThrow(
                () -> new BoardException(ErrorCode.BOARD_NOT_FOUND));
    }

    /**
     * 게시글 수정 로직
     */
    @Transactional
    public Board updateBoard(BoardUpdateRequest request, Long memberId, Long boardId) {

        Board board =
                boardRepository.findByIdAndDeleted(boardId, false).orElseThrow(
                        () -> new BoardException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardException(ErrorCode.UPDATE_BOARD_ACCESS_DENIED);
        }

        board.updateBoard(
                request.getGameMode(),
                request.getMainP(),
                request.getSubP(),
                request.getWantP(),
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
                boardRepository.findByIdAndDeleted(boardId, false).orElseThrow(
                        () -> new BoardException(ErrorCode.BOARD_NOT_FOUND));

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
        Pageable pageable = PageRequest.of(pageIdx - 1, MY_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "activityTime"));
        return boardRepository.findByMemberIdAndDeletedFalse(memberId, pageable);
    }

    /**
     * 내가 작성한 게시글(cursor) 조회
     */
    public Slice<Board> getMyBoards(Long memberId, LocalDateTime cursor) {
        Pageable pageable = PageRequest.of(0, MY_PAGE_SIZE);
        return boardRepository.findByMemberIdAndActivityTimeLessThan(memberId, cursor, pageable);
    }

    /**
     * Board 저장
     */
    @Transactional
    public Board saveBoard(Board board) {
        return boardRepository.save(board);
    }

    /**
     * 끌올 기능: 사용자가 게시글을 끌올하면 bumpTime을 현재 시간으로 업데이트
     */
    @Transactional
    public Board bumpBoard(Long boardId, Long memberId) {
        Board board = boardRepository.findByIdAndDeleted(boardId, false)
                .orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardException(ErrorCode.BUMP_ACCESS_DENIED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (board.getBumpTime() != null) {
            Duration diff = Duration.between(board.getBumpTime(), now);
            if (diff.compareTo(BUMP_INTERVAL) < 0) {
                throw new BoardException(ErrorCode.BUMP_TIME_LIMIT);
            }
        }

        board.bump(now);
        return boardRepository.save(board);
    }

    /**
     * 해당 회원이 작성한 모든 글 삭제 처리
     *
     * @param member 회원
     */
    @Transactional
    public void deleteAllBoardByMember(Member member) {
        boardRepository.deleteAllByMember(member);
    }

    /**
     * 비회원 게시글 수정
     */
    @Transactional
    public Board updateGuestBoard(GuestBoardUpdateRequest request, Long boardId) {
        Board board = boardRepository.findByIdAndDeleted(boardId, false)
                .orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.isGuest()) {
            throw new BoardException(ErrorCode.GUEST_BOARD_ACCESS_DENIED);
        }

        if (!board.verifyGuestPassword(request.getPassword())) {
            throw new BoardException(ErrorCode.INVALID_GUEST_PASSWORD);
        }

        board.updateBoard(
                request.getGameMode(),
                request.getMainP(),
                request.getSubP(),
                request.getWantP(),
                request.getMike(),
                request.getContents(),
                request.getBoardProfileImage()
        );

        return board;
    }

    /**
     * 비회원 게시글 삭제
     */
    @Transactional
    public void deleteGuestBoard(Long boardId, String password) {
        Board board = boardRepository.findByIdAndDeleted(boardId, false)
                .orElseThrow(() -> new BoardException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.isGuest()) {
            throw new BoardException(ErrorCode.GUEST_BOARD_ACCESS_DENIED);
        }

        if (!board.verifyGuestPassword(password)) {
            throw new BoardException(ErrorCode.INVALID_GUEST_PASSWORD);
        }

        board.setDeleted(true);
        boardRepository.save(board);
    }

    /**
     * 전체 게시글 커서 기반 조회 (Secondary Cursor)
     */
    public Slice<Board> getAllBoardsWithCursor(
            LocalDateTime cursor,
            Long cursorId,
            GameMode gameMode,
            Tier tier,
            Position mainP,
            Position subP) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // 페이지 기반과 동일하게 null이면 ANY로 대체
        if (mainP == null) mainP = Position.ANY;
        if (subP == null) subP = Position.ANY;

        List<Position> mainPList = new ArrayList<>();
        List<Position> subPList = new ArrayList<>();
        if (mainP == Position.ANY) {
            mainPList = Arrays.asList(Position.values());
        } else {
            mainPList.add(mainP);
        }
        if (subP == Position.ANY) {
            subPList = Arrays.asList(Position.values());
        } else {
            subPList.add(subP);
        }

        return boardRepository.findAllBoardsWithCursor(cursor, cursorId, gameMode, tier, mainPList, subPList, pageable);
    }

}
