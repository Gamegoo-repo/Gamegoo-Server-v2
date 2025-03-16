package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.matching.domain.MatchingRecord;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class MatchingFoundResponse {

    MatchingMemberInfoResponse myMatchingInfo;
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
