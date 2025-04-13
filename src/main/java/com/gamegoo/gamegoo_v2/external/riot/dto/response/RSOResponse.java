package com.gamegoo.gamegoo_v2.external.riot.dto.response;

import com.gamegoo.gamegoo_v2.account.auth.dto.response.LoginResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RSOResponse {
    Boolean isMember;
    LoginResponse loginResponse;
}
