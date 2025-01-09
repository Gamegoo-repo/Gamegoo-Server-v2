package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MannerResponse {

    int mannerLevel;
    Double mannerRank;
    int mannerRatingCount;
    List<MannerKeywordResponse> mannerKeywords;

    public static MannerResponse of(int mannerLevel, Double mannerRank, int mannerRatingCount,
                                    List<MannerKeywordResponse> mannerKeywords) {
        return MannerResponse.builder()
                .mannerLevel(mannerLevel)
                .mannerRank(mannerRank)
                .mannerRatingCount(mannerRatingCount)
                .mannerKeywords(mannerKeywords)
                .build();
    }

}
