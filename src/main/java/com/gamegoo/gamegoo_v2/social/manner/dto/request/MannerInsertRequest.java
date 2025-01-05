package com.gamegoo.gamegoo_v2.social.manner.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MannerInsertRequest {

    @NotEmpty
    List<Long> mannerKeywordIdList;

}
