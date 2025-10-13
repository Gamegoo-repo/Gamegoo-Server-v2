package com.gamegoo.gamegoo_v2.social.friend.dto;

import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendInfoResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long memberId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String name;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int profileImg;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean isLiked;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean isBlind;

    public static FriendInfoResponse of(Friend friend) {
        String name = friend.getToMember().getBlind() ? "(탈퇴한 사용자)" : friend.getToMember().getGameName();
        return FriendInfoResponse.builder()
                .memberId(friend.getToMember().getId())
                .profileImg(friend.getToMember().getProfileImage())
                .name(name)
                .isLiked(friend.isLiked())
                .isBlind(friend.getToMember().getBlind())
                .build();
    }

}
