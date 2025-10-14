package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class MatchingFoundResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    MatchingMemberInfoResponse myMatchingInfo;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    MatchingMemberInfoResponse targetMatchingInfo;

    public static MatchingFoundResponse of(MatchingRecord matchingRecord, MatchingRecord targetMatchingRecord) {
        MatchingMemberInfoResponse myMatchingMemberInfoResponse =
                MatchingMemberInfoResponse.of(matchingRecord.getMember(), matchingRecord.getMatchingUuid(),matchingRecord.getGameMode());
        MatchingMemberInfoResponse targetMatchingMemberInfoResponse =
                MatchingMemberInfoResponse.of(targetMatchingRecord.getMember(), targetMatchingRecord.getMatchingUuid(),targetMatchingRecord.getGameMode());

        return MatchingFoundResponse.builder()
                .myMatchingInfo(myMatchingMemberInfoResponse)
                .targetMatchingInfo(targetMatchingMemberInfoResponse)
                .build();
    }

}
