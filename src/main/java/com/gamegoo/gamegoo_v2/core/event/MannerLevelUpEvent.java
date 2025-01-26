package com.gamegoo.gamegoo_v2.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MannerLevelUpEvent {

    Long memberId;
    int mannerLevel;

}
