package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
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
    private final MemberRecentStatsRepository memberRecentStatsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshChampionStats(Member member) {
        Long memberId = member.getId();
        Member freshMember = memberService.findMemberById(memberId);
        
        String gameName = freshMember.getGameName();
        String tag = freshMember.getTag();
        String puuid = freshMember.getPuuid() != null ? freshMember.getPuuid() : riotAuthService.getPuuid(gameName, tag);
        memberChampionRepository.deleteByMember(freshMember);
        List<ChampionStats> preferChampionStats = riotRecordService.getPreferChampionfromMatch(gameName, puuid);
        memberChampionService.saveMemberChampions(freshMember, preferChampionStats);

        // 최근 30게임 통계 계산 및 저장
        var recStats = riotRecordService.getRecent30GameStats(gameName, puuid);
        MemberRecentStats memberRecentStats = memberRecentStatsRepository.findById(memberId)
            .orElse(MemberRecentStats.builder().member(freshMember).build());
        memberRecentStats.update(
            recStats.getRecTotalWins(),
            recStats.getRecTotalLosses(),
            recStats.getRecWinRate(),
            recStats.getRecAvgKDA(),
            recStats.getRecAvgKills(),
            recStats.getRecAvgDeaths(),
            recStats.getRecAvgAssists(),
            recStats.getRecAvgCsPerMinute(),
            recStats.getRecTotalCs()
        );
        memberRecentStatsRepository.save(memberRecentStats);
    }
} 