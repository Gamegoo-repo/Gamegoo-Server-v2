package com.gamegoo.gamegoo_v2.content.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardResponse {

    Integer totalPage;
    Integer totalCount;
    List<BoardListResponse> boards;

    public static BoardResponse of(Integer totalPage, Integer totalCount, List<BoardListResponse> boards) {
        return BoardResponse.builder()
                .totalPage(totalPage)
                .totalCount(totalCount)
                .boards(boards)
                .build();
    }

}
