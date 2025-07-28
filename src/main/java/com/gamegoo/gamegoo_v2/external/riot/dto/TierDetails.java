package com.gamegoo.gamegoo_v2.external.riot.dto;

import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierDetails {

    @Schema(ref = "#/components/schemas/GameMode")
    GameMode gameMode;
    @Schema(ref = "#/components/schemas/Tier")
    Tier tier;
    double winrate;
    int rank;
    int gameCount;

}
