package com.gamegoo.gamegoo_v2.content.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GuestBoardInsertRequest extends BoardInsertRequest {

    @NotBlank(message = "게임 닉네임은 필수입니다.")
    @Schema(description = "게임 내 닉네임")
    private String gameName;

    @NotBlank(message = "태그는 필수입니다.")
    @Schema(description = "게임 태그")
    private String tag;
}
