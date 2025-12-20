package com.gamegoo.gamegoo_v2.content.board.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class BoardUpdateRequest {

    @Schema(ref = "#/components/schemas/GameMode", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "게임 모드는 필수 값입니다.")
    GameMode gameMode;

    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "주 포지션은 필수 값입니다.")
    Position mainP;

    @Schema(ref = "#/components/schemas/Position", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "부 포지션은 필수 값입니다.")
    Position subP;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    @NotNull(message = "희망 포지션은 필수 값입니다.")
    @Size(min = 1, message = "최소 하나 이상의 희망 포지션을 선택해야 합니다.")
    List<Position> wantP;

    @Schema(ref = "#/components/schemas/Mike")
    Mike mike;

    @Schema(description = "게임 스타일 리스트 (선택, 최대 3개)")
    @Size(max = 3, message = "게임 스타일 리스트는 최대 3개까지 선택 가능합니다.")
    List<Long> gameStyles;

    @Schema(description = "게시글 내용 (선택)")
    String contents;


}
