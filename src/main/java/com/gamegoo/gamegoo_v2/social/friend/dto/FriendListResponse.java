package com.gamegoo.gamegoo_v2.social.friend.dto;

import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FriendListResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FriendInfoResponse> friendInfoList;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int listSize;

    public static FriendListResponse of(List<Friend> friends) {
        List<FriendInfoResponse> friendInfoResponseList = friends.stream()
                .map(FriendInfoResponse::of)
                .toList();

        return FriendListResponse.builder()
                .friendInfoList(friendInfoResponseList)
                .listSize(friendInfoResponseList.size())
                .build();
    }

}
