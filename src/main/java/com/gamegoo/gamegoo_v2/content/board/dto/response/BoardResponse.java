package com.gamegoo.gamegoo_v2.content.board.dto.response;

import com.gamegoo.gamegoo_v2.content.board.domain.Board;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardResponse {

    Integer totalPage;
    Integer totalCount;
    List<BoardListResponse> boards;

    public static BoardResponse of(Page<Board> boardPage) {
        // 전체 페이지/개수 추출
        int totalCount = (int) boardPage.getTotalElements();
        int totalPage = (boardPage.getTotalPages() == 0) ? 1 : boardPage.getTotalPages();

        // Board -> BoardListResponse 변환
        List<BoardListResponse> boardList = boardPage.getContent().stream()
                .map(BoardListResponse::of)
                .collect(Collectors.toList());

        // DTO 생성
        return BoardResponse.builder()
                .totalPage(totalPage)
                .totalCount(totalCount)
                .boards(boardList)
                .build();
    }

}
