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
                MatchingMemberInfoResponse.of(matchingRecord.getMember(), matchingRecord.getMatchingUuid());
        MatchingMemberInfoResponse targetMatchingMemberInfoResponse =
                MatchingMemberInfoResponse.of(targetMatchingRecord.getMember(), targetMatchingRecord.getMatchingUuid());

        return MatchingFoundResponse.builder()
                .myMatchingInfo(myMatchingMemberInfoResponse)
                .targetMatchingInfo(targetMatchingMemberInfoResponse)
                .build();
    }

}
