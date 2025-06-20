package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardCursorResponse {
    private final List<BoardListResponse> boards;
    private final boolean hasNext;
    private final LocalDateTime nextCursor;
    private Long cursorId;

    public static BoardCursorResponse of(Slice<Board> boardSlice) {
        List<BoardListResponse> boards = boardSlice.getContent().stream()
                .map(BoardListResponse::of)
                .collect(Collectors.toList());
        Long cursorId = null;
        if (!boardSlice.isEmpty()) {
            Board lastBoard = boardSlice.getContent().get(boardSlice.getContent().size() - 1);
            cursorId = lastBoard.getId();
        }
        return BoardCursorResponse.builder()
                .boards(boards)
                .hasNext(boardSlice.hasNext())
                .cursorId(cursorId)
                .build();
    }
} 