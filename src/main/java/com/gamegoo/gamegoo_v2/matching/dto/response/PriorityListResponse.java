package com.gamegoo.gamegoo_v2.matching.dto.response;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.matching.dto.PriorityValue;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PriorityListResponse {

    List<PriorityValue> myPriorityList;
    List<PriorityValue> otherPriorityList;
    MatchingMemberInfoResponse myMatchingInfo;

    public static PriorityListResponse of(List<PriorityValue> mypriorityList, List<PriorityValue> otherpriorityList,
                                          Member member, String matchingUuid) {
        MatchingMemberInfoResponse matchingMemberInfoResponse = MatchingMemberInfoResponse.of(member, matchingUuid);

        return PriorityListResponse.builder()
                .myPriorityList(mypriorityList)
                .otherPriorityList(otherpriorityList)
                .myMatchingInfo(matchingMemberInfoResponse)
                .build();
    }

}
