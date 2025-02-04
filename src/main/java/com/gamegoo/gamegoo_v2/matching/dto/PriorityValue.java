package com.gamegoo.gamegoo_v2.matching.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Builder
@ToString
public class PriorityValue {

    private final Long memberId;
    private final String matchingUuid;
    private final int priorityValue;

    public static PriorityValue of(Long memberId, String matchingUuid, int priorityValue) {
        return PriorityValue.builder()
                .memberId(memberId)
                .matchingUuid(matchingUuid)
                .priorityValue(priorityValue)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PriorityValue that = (PriorityValue) o;
        return priorityValue == that.priorityValue &&
                Objects.equals(memberId, that.memberId) &&
                Objects.equals(matchingUuid, that.matchingUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, matchingUuid, priorityValue);
    }

}
