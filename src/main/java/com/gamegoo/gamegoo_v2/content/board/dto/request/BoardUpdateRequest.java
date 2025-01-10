package com.gamegoo.gamegoo_v2.content.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class BoardUpdateRequest {

    @Schema(description = "게시글 프로필 이미지 (선택)")
    Integer boardProfileImage;

    @Min(value = 1, message = "게임 모드는 1 이상이어야 합니다.")
    @Max(value = 4, message = "게임 모드는 4 이하여야 합니다.")
    Integer gameMode;

    @NotNull(message = "주 포지션은 필수 값입니다.")
    @Min(value = 0, message = "주 포지션은 0 이상이어야 합니다.")
    @Max(value = 5, message = "주 포지션은 5 이하여야 합니다.")
    Integer mainPosition;

    @Min(value = 0, message = "부 포지션은 0 이상이어야 합니다.")
    @Max(value = 5, message = "부 포지션은 5 이하여야 합니다.")
    Integer subPosition;

    @Min(value = 0, message = "희망 포지션은 0 이상이어야 합니다.")
    @Max(value = 5, message = "희망 포지션은 5 이하여야 합니다.")
    Integer wantPosition;

    @Schema(description = "마이크 사용 여부 (선택)")
    Boolean mike;

    @Schema(description = "게임 스타일 리스트 (선택)")
    @Size(max = 3, message = "게임 스타일은 3개 이하여야 합니다.")
    List<Long> gameStyles;

    @Schema(description = "게시글 내용 (선택)")
    String contents;


}
