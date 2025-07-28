package com.gamegoo.gamegoo_v2.account.member.service;

import com.gamegoo.gamegoo_v2.account.member.domain.Member;
import com.gamegoo.gamegoo_v2.account.member.domain.MemberRecentStats;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberChampionRepository;
import com.gamegoo.gamegoo_v2.account.member.repository.MemberRecentStatsRepository;
import com.gamegoo.gamegoo_v2.external.riot.domain.ChampionStats;
import com.gamegoo.gamegoo_v2.external.riot.dto.TierDetails;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotAuthService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotInfoService;
import com.gamegoo.gamegoo_v2.external.riot.service.RiotRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChampionStatsRefreshService {

    private final RiotAuthService riotAuthService;
    private final MemberChampionService memberChampionService;
    private final MemberChampionRepository memberChampionRepository;
    private final RiotRecordService riotRecordService;
    private final RiotInfoService riotInfoService;
    private final MemberService memberService;
    private final MemberRecentStatsRepository memberRecentStatsRepository;

    @Transactional
    public void refreshChampionStats(Member member) {
        Long memberId = member.getId();
        Member freshMember = memberService.findMemberById(memberId);

        String gameName = freshMember.getGameName();
        String tag = freshMember.getTag();
        String puuid = freshMember.getPuuid() != null ? freshMember.getPuuid() : riotAuthService.getPuuid(gameName, tag);

        try {
            // 먼저 새로운 데이터 조회
            var accountInfo = riotAuthService.getAccountByPuuid(puuid);
            List<ChampionStats> preferChampionStats = riotRecordService.getPreferChampionfromMatch(gameName, puuid);
            var recStats = riotRecordService.getRecent30GameStats(gameName, puuid);
            List<TierDetails> tierWinrateRank = riotInfoService.getTierWinrateRank(puuid);

            // API 호출이 성공한 경우에만 기존 데이터 삭제 후 새로 저장
            memberChampionRepository.deleteByMember(freshMember);
            memberChampionService.saveMemberChampions(freshMember, preferChampionStats);
            freshMember.updateRiotBasicInfo(accountInfo.getGameName(), accountInfo.getTagLine());
            freshMember.updateRiotStats(tierWinrateRank);

            // 갱신 시간도 함께 업데이트
            freshMember.updateChampionStatsRefreshedAt();

            // 최근 30게임 통계 계산 및 저장
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
        } catch (Exception e) {
            // Riot API 호출 실패 시 기존 데이터 유지 (롤백)
            throw new RuntimeException("Riot API 호출 실패로 인한 챔피언 통계 업데이트 실패", e);
        }
    }
}
