package com.gamegoo.gamegoo_v2.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocketJoinEvent {

    private final Long memberId;
    private final String uuid;

}
