package com.gamegoo.gamegoo_v2.external.riot.dto;

import com.gamegoo.gamegoo_v2.account.member.domain.Tier;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TierDetails {

    GameMode gameMode;
    Tier tier;
    double winrate;
    int rank;
    int gameCount;

}
