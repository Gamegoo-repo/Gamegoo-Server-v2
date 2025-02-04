package com.gamegoo.gamegoo_v2.content.board.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardBumpResponse {

    private Long boardId;
    private LocalDateTime bumpTime;

    public static BoardBumpResponse of(Long boardId, LocalDateTime bumpTime) {
        BoardBumpResponse response = new BoardBumpResponse();
        response.boardId = boardId;
        response.bumpTime = bumpTime;
        return response;
    }

}
