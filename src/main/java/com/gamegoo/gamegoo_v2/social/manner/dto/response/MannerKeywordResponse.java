package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MannerKeywordResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long mannerKeywordId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int count;

    public static MannerKeywordResponse of(Long MannerKeywordId, int count) {
        return MannerKeywordResponse.builder()
                .mannerKeywordId(MannerKeywordId)
                .count(count)
                .build();
    }

}
