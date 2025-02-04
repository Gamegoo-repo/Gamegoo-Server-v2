package com.gamegoo.gamegoo_v2.matching.dto.request;

import com.gamegoo.gamegoo_v2.account.member.domain.Mike;
import com.gamegoo.gamegoo_v2.account.member.domain.Position;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.domain.MatchingType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InitializingMatchingRequest {

    @NotNull(message = "gameMode 는 비워둘 수 없습니다.")
    GameMode gameMode;

    @NotNull(message = "mike 는 비워둘 수 없습니다.")
    Mike mike;

    @NotNull(message = "matchingType은 비워둘 수 없습니다.")
    MatchingType matchingType;

    Position mainP;

    Position subP;

    Position wantP;

    List<Long> gameStyleIdList;

}
