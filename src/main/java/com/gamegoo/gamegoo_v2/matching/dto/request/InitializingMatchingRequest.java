package com.gamegoo.gamegoo_v2.matching.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InitializingMatchingRequest {

    @Schema(ref = "#/components/schemas/GameMode", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "gameMode 는 비워둘 수 없습니다.")
    GameMode gameMode;

    @Schema(ref = "#/components/schemas/Mike", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "mike 는 비워둘 수 없습니다.")
    Mike mike;

    @Schema(ref = "#/components/schemas/MatchingType", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "matchingType은 비워둘 수 없습니다.")
    MatchingType matchingType;

    @Schema(ref = "#/components/schemas/Position")
    Position mainP;

    @Schema(ref = "#/components/schemas/Position")
    Position subP;

    @ArraySchema(schema = @Schema(ref = "#/components/schemas/Position"))
    List<Position> wantP;

    List<Long> gameStyleIdList;

}
