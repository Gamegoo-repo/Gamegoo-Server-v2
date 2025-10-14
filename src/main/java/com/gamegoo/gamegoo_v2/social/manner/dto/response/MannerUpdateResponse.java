package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MannerUpdateResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long mannerRatingId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long targetMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> mannerKeywordIdList;

    public static MannerUpdateResponse of(MannerRating mannerRating, List<Long> mannerKeywordIdList) {
        return MannerUpdateResponse.builder()
                .mannerRatingId(mannerRating.getId())
                .targetMemberId(mannerRating.getToMember().getId())
                .mannerKeywordIdList(mannerKeywordIdList)
                .build();
    }

}
