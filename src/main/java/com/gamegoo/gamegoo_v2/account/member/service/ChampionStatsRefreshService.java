package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChampionStatsRefreshService {

    private final RiotAuthService riotAuthService;
    private final MemberChampionService memberChampionService;
    private final MemberChampionRepository memberChampionRepository;
    private final RiotRecordService riotRecordService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshChampionStats(Member member) {
        String gameName = member.getGameName();
        String tag = member.getTag();
        String puuid = riotAuthService.getPuuid(gameName, tag);
        memberChampionRepository.deleteByMember(member);
        List<ChampionStats> preferChampionStats = riotRecordService.getPreferChampionfromMatch(gameName, puuid);
        memberChampionService.saveMemberChampions(member, preferChampionStats);
    }
} 