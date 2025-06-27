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
    private final MemberService memberService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshChampionStats(Member member) {
        Long memberId = member.getId();
        Member freshMember = memberService.findMemberById(memberId);
        
        String gameName = freshMember.getGameName();
        String tag = freshMember.getTag();
        String puuid = riotAuthService.getPuuid(gameName, tag);
        memberChampionRepository.deleteByMember(freshMember);
        List<ChampionStats> preferChampionStats = riotRecordService.getPreferChampionfromMatch(gameName, puuid);
        memberChampionService.saveMemberChampions(freshMember, preferChampionStats);
    }
} 