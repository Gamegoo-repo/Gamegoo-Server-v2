package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MannerInsertResponse {

    Long mannerRatingId;
    Long targetMemberId;
    List<Long> mannerKeywordIdList;

    public static MannerInsertResponse of(MannerRating mannerRating, List<Long> mannerKeywordIdList) {
        return MannerInsertResponse.builder()
                .mannerRatingId(mannerRating.getId())
                .targetMemberId(mannerRating.getToMember().getId())
                .mannerKeywordIdList(mannerKeywordIdList)
                .build();
    }

}
