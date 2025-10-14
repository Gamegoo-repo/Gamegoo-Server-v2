package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardCursorResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final List<BoardListResponse> boards;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final boolean hasNext;
    private final LocalDateTime nextCursor;
    private Long cursorId;

    public static BoardCursorResponse of(Slice<Board> boardSlice) {
        List<BoardListResponse> boards = boardSlice.getContent().stream()
                .map(BoardListResponse::of)
                .collect(Collectors.toList());
        Long cursorId = null;
        LocalDateTime nextCursor = null;
        if (!boardSlice.isEmpty()) {
            Board lastBoard = boardSlice.getContent().get(boardSlice.getContent().size() - 1);
            cursorId = lastBoard.getId();
            nextCursor = lastBoard.getActivityTime();
        }
        return BoardCursorResponse.builder()
                .boards(boards)
                .hasNext(boardSlice.hasNext())
                .cursorId(cursorId)
                .nextCursor(nextCursor)
                .build();
    }
} 
