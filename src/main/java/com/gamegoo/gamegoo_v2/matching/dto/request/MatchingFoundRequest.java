package com.gamegoo.gamegoo_v2.matching.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingFoundRequest {

    @NotNull
    String matchingUuid;

    @NotNull
    String targetMatchingUuid;

}
