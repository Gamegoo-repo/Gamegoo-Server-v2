package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiotPuuidGameNameResponse {

    String puuid;
    String gameName;
    String tagLine;

}
