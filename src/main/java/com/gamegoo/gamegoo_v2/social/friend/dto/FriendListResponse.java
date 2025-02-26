package com.gamegoo.gamegoo_v2.social.friend.dto;

import com.gamegoo.gamegoo_v2.social.friend.domain.Friend;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FriendListResponse {

    List<FriendInfoResponse> friendInfoList;
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
