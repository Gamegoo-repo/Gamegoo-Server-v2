package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MannerKeywordResponse {

    Long mannerKeywordId;
    int count;

    public static MannerKeywordResponse of(Long MannerKeywordId, int count) {
        return MannerKeywordResponse.builder()
                .mannerKeywordId(MannerKeywordId)
                .count(count)
                .build();
    }

}
