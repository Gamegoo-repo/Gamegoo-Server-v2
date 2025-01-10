package com.gamegoo.gamegoo_v2.content.board.service;

import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import com.gamegoo.gamegoo_v2.content.board.domain.BoardGameStyle;
import com.gamegoo.gamegoo_v2.core.exception.BoardException;
import com.gamegoo.gamegoo_v2.core.exception.common.ErrorCode;
import com.gamegoo.gamegoo_v2.game.domain.GameStyle;
import com.gamegoo.gamegoo_v2.game.repository.GameStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GameStyle과 관련된 로직을 처리하는 Service 레이어
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardGameStyleService {

    private final GameStyleRepository gameStyleRepository;

    /**
     * 요청으로 들어온 gameStyleIds(List<Long>)를 조회하여
     * BoardGameStyle 엔티티를 생성 후 Board에 추가
     */
    @Transactional
    public void mapGameStylesToBoard(Board board, List<Long> gameStyleIds) {
        List<BoardGameStyle> boardGameStyles = gameStyleIds.stream()
                .map(this::findGameStyle)
                .map(gameStyle -> BoardGameStyle.create(gameStyle, board))
                .collect(Collectors.toList());

        boardGameStyles.forEach(board::addBoardGameStyle);
    }

    /**
     * 단건 GameStyle 조회
     */
    private GameStyle findGameStyle(Long gameStyleId) {
        return gameStyleRepository.findById(gameStyleId)
                .orElseThrow(() -> new BoardException(ErrorCode.BOARD_GAME_STYLE_NOT_FOUND));
    }

    @Transactional
    public void updateBoardGameStyles(Board board, List<Long> gameStyleIds) {
        if (gameStyleIds == null) {
            // 게임스타일 수정이 없을 경우, 그냥 리턴
            return;
        }

        // 1) DB에서 GameStyle 엔티티 찾기
        List<GameStyle> newGameStyleList = gameStyleIds.stream()
                .map(this::findGameStyle)
                .collect(Collectors.toList());

        // 2) 기존 BoardGameStyle 매핑
        Map<Long, BoardGameStyle> existingMap = board.getBoardGameStyles().stream()
                .collect(Collectors.toMap(
                        bgs -> bgs.getGameStyle().getId(),
                        bgs -> bgs
                ));

        // 3) 새로 들어온 집합
        Set<Long> newIds = newGameStyleList.stream()
                .map(GameStyle::getId)
                .collect(Collectors.toSet());

        // 4) 제거 대상 찾기
        List<BoardGameStyle> toRemove = board.getBoardGameStyles().stream()
                .filter(bgs -> !newIds.contains(bgs.getGameStyle().getId()))
                .collect(Collectors.toList());

        // 제거
        toRemove.forEach(board::removeBoardGameStyle);

        for (GameStyle gs : newGameStyleList) {
            if (!existingMap.containsKey(gs.getId())) {
                BoardGameStyle created = BoardGameStyle.create(gs, board);
                board.addBoardGameStyle(created);
            }
        }
    }

}
