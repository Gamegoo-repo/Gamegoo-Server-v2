package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MyBoardCursorResponse {

    List<MyBoardListResponse> myBoards;
    int size;
    boolean hasNext;
    Long nextCursor;


    public static MyBoardCursorResponse of(Slice<Board> boardSlice) {

        // Board -> MyBoardListResponse 변환
        List<MyBoardListResponse> boardList = boardSlice.getContent().stream()
                .map(MyBoardListResponse::of)
                .collect(Collectors.toList());

        Long nextCursor = boardSlice.hasNext() && !boardList.isEmpty()
                ? boardList.get(boardList.size() - 1).getBoardId()
                :null;

        // DTO 생성
        return MyBoardCursorResponse.builder()
                .myBoards(boardList)
                .size(boardList.size())
                .hasNext(boardSlice.hasNext())
                .nextCursor(nextCursor)
                .build();
    }

}
