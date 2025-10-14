package com.gamegoo.gamegoo_v2.content.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardBumpResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long boardId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime bumpTime;

    public static BoardBumpResponse of(Long boardId, LocalDateTime bumpTime) {
        BoardBumpResponse response = new BoardBumpResponse();
        response.boardId = boardId;
        response.bumpTime = bumpTime;
        return response;
    }

}
