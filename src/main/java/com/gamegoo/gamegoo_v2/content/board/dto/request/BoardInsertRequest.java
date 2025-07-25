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
public class BoardInsertRequest {

    @Min(value = 1, message = "프로필 이미지의 값은 1이상이어야 합니다.")
    @Max(value = 8, message = "프로필 이미지의 값은 8이하이어야 합니다.")
    @NotNull(message = "boardProfileImage 값은 비워둘 수 없습니다.")
    Integer boardProfileImage;

    @Schema(ref = "#/components/schemas/GameMode")
    @NotNull(message = "게임 모드는 필수 값입니다.")
    GameMode gameMode;

    @Schema(ref = "#/components/schemas/Position")
    @NotNull(message = "주 포지션은 필수 값입니다.")
    Position mainP;

    @Schema(ref = "#/components/schemas/Position")
    @NotNull(message = "부 포지션은 필수 값입니다.")
    Position subP;

    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    @NotNull(message = "희망 포지션은 필수 값입니다.")
    @Size(min = 1, message = "최소 하나 이상의 희망 포지션을 선택해야 합니다.")
    List<Position> wantP;

    @Schema(ref = "#/components/schemas/Mike", defaultValue = "UNAVAILABLE")
    Mike mike = Mike.UNAVAILABLE;

    @NotNull(message = "게임 스타일 리스트는 필수 값입니다.")
    @Size(min = 1, max = 3, message = "게임 스타일 리스트는 1개 이상 3개 이하여야 합니다.")
    List<Long> gameStyles;

    @Schema(description = "게시글 내용 (선택)")
    String contents;


}
