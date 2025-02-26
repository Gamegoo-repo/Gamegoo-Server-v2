package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MyBoardResponse {

    Integer totalPage;
    Integer totalCount;
    List<MyBoardListResponse> myBoards;

    public static MyBoardResponse of(Page<Board> boardPage) {
        // 전체 페이지/개수 추출
        int totalCount = (int) boardPage.getTotalElements();
        int totalPage = (boardPage.getTotalPages() == 0) ? 1 : boardPage.getTotalPages();

        // Board -> MyBoardListResponse 변환
        List<MyBoardListResponse> boardList = boardPage.getContent().stream()
                .map(MyBoardListResponse::of)
                .collect(Collectors.toList());

        // DTO 생성
        return MyBoardResponse.builder()
                .totalPage(totalPage)
                .totalCount(totalCount)
                .myBoards(boardList)
                .build();
    }

}
