package com.gamegoo.gamegoo_v2.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AcceptFriendRequestEvent {

    private final Long memberId;
    private final Long targetMemberId;

}
