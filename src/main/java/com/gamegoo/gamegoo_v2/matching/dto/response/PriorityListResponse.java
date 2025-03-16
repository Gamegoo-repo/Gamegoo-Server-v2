package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.domain.GameMode;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class PriorityListResponse {

    List<PriorityValue> myPriorityList;
    List<PriorityValue> otherPriorityList;
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
