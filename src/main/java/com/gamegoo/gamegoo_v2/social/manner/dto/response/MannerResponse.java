package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MannerResponse {

    int mannerLevel;
    Double mannerRank;
    int mannerRatingCount;

    public static MannerResponse of(int mannerLevel, Double mannerRank, int mannerRatingCount) {
        return MannerResponse.builder()
                .mannerLevel(mannerLevel)
                .mannerRank(mannerRank)
                .mannerRatingCount(mannerRatingCount)
                .build();
    }

}
