package com.gamegoo.gamegoo_v2.account.member.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameStyleRequest {

    List<Long> gameStyleIdList;

}
