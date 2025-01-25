package com.gamegoo.gamegoo_v2.matching.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PriorityValue {

    Long memberId;
    String matchingUuid;
    int priorityValue;

    public static PriorityValue of(Long memberId, String matchingUuid, int priorityValue) {
        return PriorityValue.builder()
                .memberId(memberId)
                .matchingUuid(matchingUuid)
                .priorityValue(priorityValue)
                .build();
    }

}
