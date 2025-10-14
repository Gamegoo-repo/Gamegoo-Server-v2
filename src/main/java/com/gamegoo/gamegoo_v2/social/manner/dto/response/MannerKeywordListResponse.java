package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MannerKeywordListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<MannerKeywordResponse> mannerKeywords;

    public static MannerKeywordListResponse of(List<MannerKeywordResponse> mannerKeywords) {
        return MannerKeywordListResponse.builder()
                .mannerKeywords(mannerKeywords)
                .build();
    }

}
