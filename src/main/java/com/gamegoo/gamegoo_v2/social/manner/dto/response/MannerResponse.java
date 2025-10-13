package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MannerResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int mannerLevel;
    Double mannerRank;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int mannerRatingCount;

    public static MannerResponse of(int mannerLevel, Double mannerRank, int mannerRatingCount) {
        return MannerResponse.builder()
                .mannerLevel(mannerLevel)
                .mannerRank(mannerRank)
                .mannerRatingCount(mannerRatingCount)
                .build();
    }

}
