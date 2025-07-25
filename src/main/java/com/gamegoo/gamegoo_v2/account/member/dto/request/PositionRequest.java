package com.gamegoo.gamegoo_v2.account.member.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PositionRequest {

    @Schema(ref = "#/components/schemas/Position")
    @NotNull(message = "메인 포지션은 null일 수 없습니다.")
    private Position mainP;

    @Schema(ref = "#/components/schemas/Position")
    @NotNull(message = "서브 포지션은 null일 수 없습니다.")
    private Position subP;

    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    @NotNull(message = "원하는 포지션은 null일 수 없습니다.")
    private List<Position> wantP;

}
