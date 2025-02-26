package com.gamegoo.gamegoo_v2.external.riot.service;

import com.gamegoo.gamegoo_v2.external.riot.dto.RiotVerifyExistUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiotFacadeService {

    private final RiotAuthService riotAccountService;

    /**
     * 사용가능한 riot 계정인지 검증
     *
     * @param request 소환사명, 태그
     */
    public String verifyRiotAccount(RiotVerifyExistUserRequest request) {
        // 1. puuid 발급 가능한지 검증
        String puuid = riotAccountService.getPuuid(request.getGameName(), request.getTag());
        
        // 2. summonerid 발급 가능한지 검증
        riotAccountService.getSummonerId(puuid);
        return "해당 Riot 계정은 존재합니다";
    }

}
