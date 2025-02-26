package com.gamegoo.gamegoo_v2.social.block.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlockResponse {

    Long targetMemberId;
    String message;

    public static BlockResponse of(Long targetMemberId, String message) {
        return BlockResponse.builder()
                .targetMemberId(targetMemberId)
                .message(message)
                .build();
    }

}
