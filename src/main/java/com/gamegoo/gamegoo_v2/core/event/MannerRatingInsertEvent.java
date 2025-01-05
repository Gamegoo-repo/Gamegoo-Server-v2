package com.gamegoo.gamegoo_v2.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MannerRatingInsertEvent {

    Long memberId;
    List<Long> mannerKeywordIdList;

}
