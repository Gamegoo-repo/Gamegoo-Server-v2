package com.gamegoo.gamegoo_v2.social.block.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlockResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long targetMemberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message;

    public static BlockResponse of(Long targetMemberId, String message) {
        return BlockResponse.builder()
                .targetMemberId(targetMemberId)
                .message(message)
                .build();
    }

}
