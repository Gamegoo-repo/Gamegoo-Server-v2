package com.gamegoo.gamegoo_v2.social.manner.dto.response;

import com.gamegoo.gamegoo_v2.social.manner.domain.MannerRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class MannerRatingResponse {

    Long mannerRatingId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> mannerKeywordIdList;

    public static MannerRatingResponse of(Optional<MannerRating> mannerRating) {
        Long mannerRatingId = mannerRating.map(MannerRating::getId).orElse(null);
        List<Long> mannerKeywordIdList = mannerRating
                .map(mr -> mr.getMannerRatingKeywordList().stream()
                        .map(mrk -> mrk.getMannerKeyword().getId())
                        .toList())
                .orElse(List.of());

        return MannerRatingResponse.builder()
                .mannerRatingId(mannerRatingId)
                .mannerKeywordIdList(mannerKeywordIdList)
                .build();
    }

}
