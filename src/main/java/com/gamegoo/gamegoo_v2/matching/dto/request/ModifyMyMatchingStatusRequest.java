package com.gamegoo.gamegoo_v2.matching.dto.request;

import com.gamegoo.gamegoo_v2.matching.domain.MatchingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModifyMyMatchingStatusRequest {

    @NotNull
    String matchingUuid;

    @NotNull
    MatchingStatus status;


}
