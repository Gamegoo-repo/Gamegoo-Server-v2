package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class PriorityListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PriorityValue> myPriorityList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PriorityValue> otherPriorityList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    MatchingMemberInfoResponse myMatchingInfo;

    public static PriorityListResponse of(List<PriorityValue> mypriorityList, List<PriorityValue> otherpriorityList,
                                          Member member, String matchingUuid, GameMode gameMode) {
        MatchingMemberInfoResponse matchingMemberInfoResponse = MatchingMemberInfoResponse.of(member, matchingUuid,gameMode);

        return PriorityListResponse.builder()
                .myPriorityList(mypriorityList)
                .otherPriorityList(otherpriorityList)
                .myMatchingInfo(matchingMemberInfoResponse)
                .build();
    }

}
