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
public class MyBoardCursorResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<MyBoardListResponse> myBoards;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int size;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean hasNext;
    LocalDateTime nextCursor;

    public static MyBoardCursorResponse of(Slice<Board> boardSlice) {
        // Board -> MyBoardListResponse 변환
        List<MyBoardListResponse> boardList = boardSlice.getContent().stream()
                .map(MyBoardListResponse::of)
                .collect(Collectors.toList());

        LocalDateTime nextCursor = boardSlice.hasNext() && !boardList.isEmpty()
                ? boardSlice.getContent().get(boardSlice.getContent().size() - 1).getActivityTime()
                : null;

        // DTO 생성
        return MyBoardCursorResponse.builder()
                .myBoards(boardList)
                .size(boardList.size())
                .hasNext(boardSlice.hasNext())
                .nextCursor(nextCursor)
                .build();
    }
}
